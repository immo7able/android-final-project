package com.example.final_project.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ProfileFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var db: Database

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.profile_fragment, container, false)

        sharedPreferences = requireContext().getSharedPreferences("userPrefs", Context.MODE_PRIVATE)

        db = Room.databaseBuilder(
            requireContext().applicationContext,
            Database::class.java,
            "notes_database"
        ).build()

        usernameEditText = rootView.findViewById(R.id.editTextUsername)
        passwordEditText = rootView.findViewById(R.id.editTextPassword)
        saveProfileButton = rootView.findViewById(R.id.buttonSaveProfile)
        logoutButton = rootView.findViewById(R.id.buttonLogout)

        loadUserProfile()

        saveProfileButton.setOnClickListener {
            saveUserProfile()
        }

        logoutButton.setOnClickListener {
            logout()
        }

        return rootView
    }

    private fun loadUserProfile() {
        val username = sharedPreferences.getString("username", "") ?: ""
        val password = sharedPreferences.getString("password", "") ?: ""

        usernameEditText.setText(username)
        passwordEditText.setText(password)
    }

    private fun saveUserProfile() {
        val newUsername = usernameEditText.text.toString()
        val newPassword = passwordEditText.text.toString()

        if (newUsername.isBlank() || newPassword.isBlank()) {
            Toast.makeText(requireContext(), "Поля не могут быть пустыми!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getUserByUsername(newUsername)
            if (existingUser != null && existingUser.username != sharedPreferences.getString("username", "")) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Имя пользователя уже занято", Toast.LENGTH_SHORT).show()
                }
            } else {
                val id = sharedPreferences.getLong("userId", 0L)
                val user = User(id = id, username = newUsername, password = newPassword)
                db.userDao().update(user)
                withContext(Dispatchers.Main) {
                    with(sharedPreferences.edit()) {
                        putString("username", newUsername)
                        putString("password", newPassword)
                        apply()
                    }
                    Toast.makeText(requireContext(), "Данные сохранены", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logout() {
        with(sharedPreferences.edit()) {
            remove("username")
            remove("password")
            remove("userId")
            apply()
        }

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }
}
