package com.smart.smarttodo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.smart.smarttodo.fragments.GoogleAuthFragment
import com.smart.smarttodo.fragments.HomeFragment
import com.smart.smarttodo.fragments.SplashFragment
import com.smart.smarttodo.utils.model.ToDoData


class MainActivity : AppCompatActivity() , GoogleAuthFragment.OnBackPressedListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    companion object {
        const val RC_SIGN_IN = 120
        lateinit var toDoItemList: MutableList<ToDoData>
    }

    override fun onFragmentBackPressed(fragmentName: String) {
        Log.d("FragmentBackPressed", "Fragment name: $fragmentName")
    }

    override fun onBackPressed() {
        super.onBackPressed()

        val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
        if (fragment is GoogleAuthFragment) {
            fragment.closeAllFragments()
        }
        else if (fragment is SplashFragment) {
            fragment.closeAllFragments()
        }
        else if (fragment is HomeFragment) {
            fragment.closeAllFragments()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        else {
            super.onBackPressed()
        }
    }

}