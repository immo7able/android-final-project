package com.example.final_project.fragment

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.room.Room
import com.example.final_project.R
import com.example.final_project.config.Database
import com.example.final_project.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterFragment : Fragment(R.layout.register_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameEditText = view.findViewById<EditText>(R.id.etUsername)
        val passwordEditText = view.findViewById<EditText>(R.id.etPassword)
        val registerButton = view.findViewById<Button>(R.id.btnRegister)
        val loginButton = view.findViewById<Button>(R.id.btnLogin)
        val db = Room.databaseBuilder(
            requireContext().applicationContext,
            Database::class.java,
            "notes_database"
        ).build()

        loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val existingUser = db.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Username already taken", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val newUser = User(username = username, password = password)
                    db.userDao().insert(newUser)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "User registered successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                }
            }
        }
    }
}
