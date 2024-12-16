package com.example.final_project.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.final_project.R
import com.example.final_project.config.Database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment(R.layout.login_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = view.findViewById<EditText>(R.id.etLoginUsername)
        val passwordEditText = view.findViewById<EditText>(R.id.etLoginPassword)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            Database::class.java,
            "notes_database"
        ).fallbackToDestructiveMigration().build()

        val navController = findNavController()

        registerButton.setOnClickListener {
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotBlank() && password.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val user = db.userDao().getUserByUsername(username)
                    withContext(Dispatchers.Main) {
                        if (user != null && user.password == password) {
                            val sharedPreferences =
                                requireContext().getSharedPreferences("userPrefs", AppCompatActivity.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()

                            user.id?.let { it1 ->
                                editor.putLong("userId", it1);
                                editor.putString("username", user.username)
                                editor.putString("password", user.password)
                            }
                            editor.apply()

                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()
                            navController.navigate(R.id.action_loginFragment_to_mainFragment)
                        } else {
                            Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
