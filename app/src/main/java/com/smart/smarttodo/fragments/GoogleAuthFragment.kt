package com.smart.smarttodo.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.smart.smarttodo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.smart.smarttodo.MainActivity
import com.smart.smarttodo.databinding.FragmentGoogleAuthBinding


class GoogleAuthFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentGoogleAuthBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    fun closeAllFragments() {
        // Pop the back stack to close all fragments
//        findNavController().popBackStack()
        requireActivity().supportFragmentManager.popBackStack()

    }

    interface OnBackPressedListener {
        fun onFragmentBackPressed(fragmentName: String)
    }
    private lateinit var onBackPressedListener: OnBackPressedListener
    private val fragmentName = "GoogleAuthFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentGoogleAuthBinding.inflate(inflater, container, false)

        // Initialize onBackPressedListener
        onBackPressedListener = activity as OnBackPressedListener

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        init(view)

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = context?.let { GoogleSignIn.getClient(it, gso) }!!

        binding.signInBtn.setOnClickListener {
            signIn()
        }

    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,
            MainActivity.RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == MainActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    if (user != null) {
                        if (isGoogleAccountAdded()){
                            openNewActivity()
                        }
                        else{
                            saveUserInfoToDatabase(user.uid, user.displayName, user.email, user.photoUrl.toString())
                        }

                        // Save user information to Realtime Database
                    } else {
                        Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // If sign-in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun isGoogleAccountAdded(): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            for (userInfo in currentUser.providerData) {
                if (userInfo.providerId == "google.com") {
                    // Google account is already added to Firebase Authentication
                    return true
                }
            }
        }
        // Google account is not added to Firebase Authentication
        return false
    }

    private fun saveUserInfoToDatabase(
        userId: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        val usersRef = FirebaseDatabase.getInstance().getReference("todo_users")
        val userMap = HashMap<String, Any>()
        userMap["userId"] = userId
        userMap["displayName"] = displayName ?: ""
        userMap["email"] = email ?: ""
        userMap["photoUrl"] = photoUrl ?: ""

        usersRef.child(userId).setValue(userMap)

        openNewActivity()
    }

    private fun openNewActivity() {
        navController.navigate(R.id.action_googleAuthFragment_to_homeFragment)
    }

    private fun init(view: View) {
        navController = Navigation.findNavController(view)
        firebaseAuth = FirebaseAuth.getInstance()
    }
}