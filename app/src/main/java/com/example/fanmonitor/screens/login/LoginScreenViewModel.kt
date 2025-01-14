package com.example.fanmonitor.screens.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel: ViewModel() {
    //val loadingState = MutableStateFlow(LoadingState.IDLE)
    private val auth: FirebaseAuth = Firebase.auth

    private val _loading= MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private fun createUser(displayName: String?) {
        val userID = auth.currentUser?.uid
        val user = mutableMapOf<String, Any>()
        user["user_id"] = userID.toString()
        user["display_name"] = displayName.toString()
        FirebaseFirestore.getInstance().collection("users")
            .add(user)
    }

    fun createUserWithEmailAndPassword(email: String, password: String, home: () -> Unit){
        if(_loading.value == false){
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val displayName = task.result.user?.email?.split('@')?.get(0)
                        createUser(displayName)
                        home()
                    } else {
                        Log.d("FB", "signInWithEmailAndPassword: ${task.result.toString()}")
                    }
                    _loading.value = false
                }
        }
    }

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit) = viewModelScope.launch{
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("FB", "signInWithEmailAndPassword: ${task.result.toString()}")
                        _loading.value = false
                        home()
                    } else {
                        //TODO: handle error
                    }
                }
        }
        catch (ex: Exception){
            Log.d("FB", "signInWithEmailAndPassword: ${ex.message}")
        }
    }

}