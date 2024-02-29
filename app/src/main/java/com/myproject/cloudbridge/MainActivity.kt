package com.myproject.cloudbridge

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.user.UserApiClient
import com.myproject.cloudbridge.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding.button.setOnClickListener {
            kakaoLogin()
        }
    }

    fun kakaoLogin(){
        // 카카오계정으로 로그인 공통 callback 구성
        // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨

        val TAG = "test1234"
        KakaoSdk.init(this, "48342662e12c5df7648b803836d9c87d")
        Log.d("test1234", "keyHash ${Utility.getKeyHash(this@MainActivity)}")
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e(TAG, "카카오계정으로 로그인 실패", error)
                // android key hash 값을 출력하고 이를 등록해야 한다.
            } else if (token != null) {
                Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")

                // 로그인한 사용자 정보를 가져온다.
                // 이 때 accessToken 을 카카오 서버로 전달해야 해야하는데 알아서해준다.
                UserApiClient.instance.me { user, error ->
                    if(error != null){
                        Log.e(TAG, "사용자 정보를 가져오는데 실패하였습니다", error)
                    } else if(user != null){
                        // PrimaryKey로 사용 가능하다
                        Log.d(TAG, "회원번호 : ${user.id}")
                        Log.d(TAG, "이메일 : ${user.kakaoAccount?.email}")
                        Log.d(TAG, "닉네임 : ${user.kakaoAccount?.profileNicknameNeedsAgreement}")
                        Log.d(TAG, "프로필사진 : ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                    }
                }
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@MainActivity)) {
            UserApiClient.instance.loginWithKakaoTalk(this@MainActivity) { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
                } else if (token != null) {
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this@MainActivity, callback = callback)
        }
    }
}