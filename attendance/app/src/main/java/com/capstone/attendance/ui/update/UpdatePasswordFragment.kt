package com.capstone.attendance.ui.update

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.capstone.attendance.R
import com.capstone.attendance.databinding.FragmentUpdatePasswordBinding
import com.capstone.attendance.utils.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class UpdatePasswordFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var updatePasswordBinding: FragmentUpdatePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        updatePasswordBinding =
            FragmentUpdatePasswordBinding.inflate(layoutInflater, container, false)
        return updatePasswordBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        updatePasswordBinding.layoutPassword.visibility = View.VISIBLE
        updatePasswordBinding.layoutNewPass.visibility = View.GONE
        updatePasswordBinding.btnAuth.setOnClickListener {
            val pass = updatePasswordBinding.etPassword.text.toString().trim()
            if (pass.isEmpty()) {
                updatePasswordBinding.txtInputPassword.error = PASSWORD_EMPTY
                updatePasswordBinding.txtInputPassword.requestFocus()
                return@setOnClickListener
            }
            user.let {
                val userCredential = EmailAuthProvider.getCredential(it?.email!!, pass)
                it.reauthenticate(userCredential).addOnCompleteListener { Task ->
                    when {
                        Task.isSuccessful -> {
                            updatePasswordBinding.layoutPassword.visibility = View.GONE
                            updatePasswordBinding.layoutNewPass.visibility = View.VISIBLE
                        }
                        Task.exception is FirebaseAuthInvalidCredentialsException -> {
                            updatePasswordBinding.txtInputPassword.error = WRONG_PASSWORD
                            updatePasswordBinding.txtInputPassword.requestFocus()
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
            updatePasswordBinding.btnUpdate.setOnClickListener btnUpdate@{ view ->
                val newPass = updatePasswordBinding.etNewPassword.text.toString().trim()
                val newPassConfirm =
                    updatePasswordBinding.etNewPasswordConfirm.text.toString().trim()
                if (newPass.isEmpty()) {
                    updatePasswordBinding.txtInputNewPassword.error = PASSWORD_EMPTY
                    updatePasswordBinding.txtInputNewPassword.requestFocus()
                    return@btnUpdate
                }
                if(newPass.length < 8){
                    updatePasswordBinding.txtInputNewPassword.error = PASSWORD_LENGTH
                    updatePasswordBinding.txtInputNewPassword.requestFocus()
                }
                if (newPass != newPassConfirm) {
                    updatePasswordBinding.txtInputNewPasswordConfirm.error = PASSWORD_NOT_SAME
                    updatePasswordBinding.txtInputNewPasswordConfirm.requestFocus()
                    return@btnUpdate
                }

                user?.let {
                    user.updatePassword(newPass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val actionUpdatedPass =
                                UpdatePasswordFragmentDirections.actionUpdatedPassword()
                            Navigation.findNavController(view).navigate(actionUpdatedPass)
                            val mNotificationManager =
                                activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            val mBuilder = NotificationCompat.Builder(
                                view.context,
                                CHANNEL_ID
                            )
                                .setSmallIcon(R.mipmap.ic_launcher_app)
                                .setLargeIcon(
                                    BitmapFactory.decodeResource(
                                        resources,
                                        R.mipmap.ic_launcher_app
                                    )
                                )
                                .setContentTitle(resources.getString(R.string.update_profile_succes))
                                .setContentText(resources.getString(R.string.update_password_succes_desc))
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
                                PASSWORD_CHANGED,
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