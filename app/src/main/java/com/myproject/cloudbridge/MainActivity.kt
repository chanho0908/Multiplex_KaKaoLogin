package com.myproject.cloudbridge

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.myproject.cloudbridge.databinding.ActivityMainBinding
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.button.setOnClickListener {
            lifecycleScope.launch {
                UserApiClient.login(this@MainActivity)
                    .collectLatest {
                        when (it) {
                            is UiState.Loading -> {}
                            is UiState.Success<*> -> {}
                            is UiState.Error -> {}
                        }
                    }
            }
        }
    }

    fun UserApiClient.Companion.login(context: Context): Flow<UiState> = callbackFlow {
        trySend(UiState.Loading)
        if (instance.isKakaoTalkLoginAvailable(context)) {
            loginWithKakaoTalk(context)
        } else {
            loginWithKakaoAccount(context)
        }
        awaitClose {
            Log.d("chanho", "awaitClose")
        }
    }

    private fun ProducerScope<UiState>.loginWithKakaoTalk(context: Context) =
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                checkLoginWithKakaoTalkError(context, error)
            } else if (token != null) {
                trySend(UiState.Success(token))
                close()
            } else {
                trySend(UiState.Error(IllegalStateException("Can't Receive Kaokao Access Token")))
                close()
            }

        }

    private fun ProducerScope<UiState>.checkLoginWithKakaoTalkError(
        context: Context,
        error: Throwable,
    ) {
        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
            trySend(UiState.Error(error))
            close()
        } else {
            loginWithKakaoAccount(context)
        }
    }

    private fun ProducerScope<UiState>.loginWithKakaoAccount(context: Context) =
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if (error != null) {
                trySend(UiState.Error(error))
            } else if (token != null) {
                trySend(UiState.Success(token))
            } else {
                trySend(UiState.Error(IllegalStateException("Can't Receive Kaokao Access Token")))
            }
            close()
        }
}