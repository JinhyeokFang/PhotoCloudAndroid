import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.*
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.photocloudandroid.Activity.MainActivity
import com.example.photocloudandroid.Activity.PictureActivity
import com.example.photocloudandroid.Model.Image
import com.example.photocloudandroid.R
import com.example.photocloudandroid.Utils.Utils
import com.example.photocloudandroid.WebClient.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_picture.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

class ImagesAdapter(private val activity: Activity, private val data: ArrayList<Image>): RecyclerView.Adapter<ImagesAdapter.SingleAlbumViewHolder>() {
    class SingleAlbumViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var galleryImage: ImageView? = null

        fun binding (activity: Activity, image: Image) {
            galleryImage = itemView.findViewById(R.id.galleryImage)

            Glide.with(activity).load(image.url).into(galleryImage)

            galleryImage!!.setOnClickListener {
                Glide.with(itemView.context).load(image.url).asBitmap().listener(object:
                    RequestListener<String, Bitmap> {
                    override fun onException(e: Exception?, model: String?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: String?, target: Target<Bitmap>?, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        val tempFile = File(itemView.context.filesDir, "temp.jpg")
                        try {
                            tempFile.createNewFile()
                            val out = FileOutputStream(tempFile)
                            resource!!.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            out.close()
                            UCrop.of(Uri.parse("file://" + tempFile.path), Uri.parse(tempFile.absolutePath))
                                .withAspectRatio(16.0f, 9.0f)
                                .start(itemView.context as MainActivity)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        return false
                    }
                }).into(galleryImage)
            }

            galleryImage!!.setOnLongClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context)
                    .setMessage("사진을 삭제하시겠습니까?")
                    .setPositiveButton("예") { dialogInterface: DialogInterface, i: Int ->
                        RetrofitClient.INSTANCE.getRetrofitService().removeImage(Utils.getSavedToken(itemView.context), image.date).enqueue(object:
                            Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                Snackbar.make(itemView, "삭제를 완료했습니다.", Snackbar.LENGTH_SHORT).show()
                            }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Snackbar.make(itemView, "삭제를 완료하기 못했습니다.", Snackbar.LENGTH_SHORT).show()
                            }
                        })
                    }
                    .setNegativeButton("아니오") { dialogInterface: DialogInterface, i: Int -> }
                builder.create().show()

                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleAlbumViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_image, parent, false)
        return SingleAlbumViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: SingleAlbumViewHolder, position: Int) {
        holder.binding(activity, data[position])
    }
}