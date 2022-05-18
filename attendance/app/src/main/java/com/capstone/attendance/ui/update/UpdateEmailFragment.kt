package com.capstone.attendance.ui.update

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.capstone.attendance.R
import com.capstone.attendance.databinding.FragmentUpdateBinding
import com.capstone.attendance.utils.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class UpdateEmailFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var updateBinding: FragmentUpdateBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        updateBinding = FragmentUpdateBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return updateBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        updateBinding.txtInputPassword.requestFocus()
        val user = auth.currentUser
        updateBinding.layoutPassword.visibility = View.VISIBLE
        updateBinding.layoutEmail.visibility = View.GONE
        updateBinding.etEmail.setText(user?.email)
        updateBinding.btnAuth.setOnClickListener {
            val pass = updateBinding.etPassword.text.toString().trim()
            if (pass.isEmpty()) {
                updateBinding.txtInputPassword.error = PASSWORD_EMPTY
                updateBinding.txtInputPassword.requestFocus()
                return@setOnClickListener
            }
            user.let {
                val userCredential = EmailAuthProvider.getCredential(it?.email!!, pass)
                it.reauthenticate(userCredential).addOnCompleteListener { Task ->
                    when {
                        Task.isSuccessful -> {
                            updateBinding.layoutPassword.visibility = View.GONE
                            updateBinding.layoutEmail.visibility = View.VISIBLE
                            updateBinding.etEmail.requestFocus()
                        }
                        Task.exception is FirebaseAuthInvalidCredentialsException -> {
                            updateBinding.txtInputPassword.error = WRONG_PASSWORD
                            updateBinding.txtInputPassword.requestFocus()
                        }
                        else -> {
                            FunctionLibrary.toast(
                                context as Activity,
                                TOAST_ERROR,
                                "${Task.exception?.message}",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(context as Activity, R.font.helveticabold)
                            )
                        }
                    }
                }
            }
            updateBinding.btnUpdate.setOnClickListener BtnUpdate@{ view ->
                val email = updateBinding.etEmail.text.toString().trim()
                if (email.isEmpty()) {
                    updateBinding.txtInputEmail.error = EMAIL_EMPTY
                    updateBinding.txtInputEmail.requestFocus()
                    return@BtnUpdate
                }
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    updateBinding.txtInputEmail.error = EMAIL_NOT_VALID
                    updateBinding.txtInputEmail.requestFocus()
                    return@BtnUpdate
                }
                user?.let {
                    user.updateEmail(email).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val actionUpdatedEmail =
                                UpdateEmailFragmentDirections.actionUpdatedEmail()
                            Navigation.findNavController(view).navigate(actionUpdatedEmail)
                            val mNotificationManager =
                                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val mBuilder = NotificationCompat.Builder(view.context, CHANNEL_ID)
                                .setSmallIcon(R.mipmap.ic_launcher_app)
                                .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.mipmap.ic_launcher_app
                                    )
                                )
                                .setContentTitle(resources.getString(R.string.update_profile_succes))
                                .setContentText(resources.getString(R.string.update_email_succes_desc))
                                .setAutoCancel(true)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val channel = NotificationChannel(
                                    CHANNEL_ID,
                                    CHANNEL_NAME,
                                    NotificationManager.IMPORTANCE_HIGH
                                )
                                channel.description = CHANNEL_NAME
                                mBuilder.setChannelId(CHANNEL_ID)
                                mNotificationManager.createNotificationChannel(channel)
                            }
                            val notification = mBuilder.build()
                            mNotificationManager.notify(NOTIFICATION_ID, notification)
                            FunctionLibrary.toast(
                                context as Activity,
                                TOAST_SUCCESS,
                                EMAIL_UPDATED,
                                MotionToastStyle.SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(context as Activity, R.font.helveticabold)
                            )
                        } else {
                            FunctionLibrary.toast(
                                context as Activity,
                                TOAST_ERROR,
                                "${it.exception?.message}",
                                MotionToastStyle.ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(context as Activity, R.font.helveticabold)
                            )
                        }
                    }
                }
            }
        }
    }
}