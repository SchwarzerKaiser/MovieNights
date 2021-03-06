package com.leewilson.moviebee.ui.main.profile.state

sealed class ProfileStateEvent {

    object FetchUserData: ProfileStateEvent()

    data class UpdateUserData(
        var displayName: String? = null,
        var email: String? = null,
        var bio: String? = null,
        var imageUri: String? = null
    ) : ProfileStateEvent()
}