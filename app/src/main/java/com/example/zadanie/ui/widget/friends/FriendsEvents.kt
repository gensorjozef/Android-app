package com.example.zadanie.ui.widget.friends

import com.example.zadanie.data.db.model.FriendItem

interface FriendsEvents {
    fun onFriendClick(friend: FriendItem)
}