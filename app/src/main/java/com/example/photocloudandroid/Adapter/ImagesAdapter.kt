import android.app.Activity
import android.content.Intent
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.photocloudandroid.Activity.MainActivity
import com.example.photocloudandroid.Activity.PictureActivity
import com.example.photocloudandroid.Model.Image
import com.example.photocloudandroid.R
import java.util.ArrayList

class ImagesAdapter(private val activity: Activity, private val data: ArrayList<Image>): BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        var holder: SingleAlbumViewHolder? = null
        if (convertView == null) {
            holder = SingleAlbumViewHolder()
            convertView = LayoutInflater.from(activity).inflate(R.layout.card_image, parent, false)

            holder.galleryImage = convertView!!.findViewById<View>(R.id.galleryImage) as ImageView
            convertView.tag = holder
        } else {
            holder = convertView.tag as SingleAlbumViewHolder
        }

        val image = data[position]

        holder.galleryImage!!.setOnClickListener {
            val intent = Intent(holder.galleryImage!!.context, PictureActivity::class.java)
            intent.putExtra("url", image.url)
            intent.putExtra("date", image.date)
            parent.context.startActivity(intent)
        }

        Glide.with(activity).load(image.url).into(holder.galleryImage!!)

        return convertView
    }

    internal inner class SingleAlbumViewHolder {
        var galleryImage: ImageView? = null
    }
}
