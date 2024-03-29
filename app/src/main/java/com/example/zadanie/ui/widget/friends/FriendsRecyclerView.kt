package com.example.zadanie.ui.widget.friends

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zadanie.data.db.model.BarItem
import com.example.zadanie.data.db.model.FriendItem
import com.example.zadanie.ui.fragments.BarsFragmentDirections
import com.example.zadanie.ui.fragments.FriendsFragmentDirections
import com.example.zadanie.ui.widget.barlist.BarsAdapter
import com.example.zadanie.ui.widget.barlist.BarsEvents
import com.example.zadanie.ui.widget.barlist.BarsRecyclerView

class FriendsRecyclerView : RecyclerView {
    private lateinit var friendsAdapter: FriendsAdapter
    /**
     * Default constructor
     *
     * @param context context for the activity
     */
    constructor(context: Context) : super(context) {
        init(context)
    }

    /**
     * Constructor for XML layout
     *
     * @param context activity context
     * @param attrs   xml attributes
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        friendsAdapter = FriendsAdapter(object : FriendsEvents {
            override fun onFriendClick(friend: FriendItem) {
                if (friend.bar_id != null) {
                    this@FriendsRecyclerView.findNavController().navigate(
                        FriendsFragmentDirections.actionDetail(friend.bar_id,
                            (0..5).random().toString()
                        )
                    )
                }
            }
        })
        adapter = friendsAdapter
    }
}

@BindingAdapter(value = ["friendItems"])
fun FriendsRecyclerView.applyItems(
    friends: List<FriendItem>?
) {
    (adapter as FriendsAdapter).items = friends ?: emptyList()
}