import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.purdue.comedia.R
import kotlinx.android.synthetic.main.post_row.view.*

// Recycler View Manager
class MainAdapter: RecyclerView.Adapter<CustomViewHolder>() {
    override fun getItemCount(): Int {
        return 3 // Arbitrary placeholder
    }

    // Initialize Row View
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.post_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    // Setup Row UI Elements
    override fun onBindViewHolder(holder: CustomViewHolder, rowIndex: Int) {
        holder.view.feedPostTitle.text = "Joke Title. Row: $rowIndex"
    }

}

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) { }