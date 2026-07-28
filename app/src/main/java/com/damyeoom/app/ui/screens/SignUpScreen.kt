package com.damyeoom.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damyeoom.app.R
import com.damyeoom.app.data.database.AppDatabase
import com.damyeoom.app.entity.User
import com.damyeoom.app.ui.theme.*
import kotlinx.coroutines.launch

// 새로운 사용자의 회원가입을 처리하는 화면
@Composable
fun SignUpScreen(
    onSignUpComplete: () -> Unit
) {
    // Room DB와 코루틴 설정
    val context = LocalContext.current
    val db = remember {
        AppDatabase.getDatabase(context)
    }
    val scope = rememberCoroutineScope()

    // 회원가입 입력값 상태
    var email by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var passwordConfirm by remember {
        mutableStateOf("")
    }
    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    // 모든 입력칸 작성 여부 확인
    val isFormFilled =
        email.isNotBlank() &&
                password.isNotBlank() &&
                passwordConfirm.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgLight)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(72.dp))

        // 앱 로고
        Image(
            painter = painterResource(
                id = R.drawable.ic_logo_damyeoom
            ),
            contentDescription = "다녀옴! 로고",
            modifier = Modifier.height(40.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "나만의 국내 여행 지도",
            fontSize = 14.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            // 이메일 입력
            Text(
                text = "이메일",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                placeholder = {
                    Text("exam@gmail.com")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardGray,
                    focusedContainerColor = CardGray,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 비밀번호 입력
            Text(
                text = "비밀번호",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                placeholder = {
                    Text("비밀번호를 입력하세요")
                },
                singleLine = true,
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardGray,
                    focusedContainerColor = CardGray,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 비밀번호 확인
            Text(
                text = "비밀번호 확인",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = passwordConfirm,
                onValueChange = {
                    passwordConfirm = it
                    errorMessage = null
                },
                placeholder = {
                    Text("비밀번호를 다시 입력하세요")
                },
                singleLine = true,
                visualTransformation =
                    PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = CardGray,
                    focusedContainerColor = CardGray,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = TextPrimary
                )
            )

            // 회원가입 오류 메시지
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = errorMessage ?: "",
                    fontSize = 12.sp,
                    color = ErrorRed
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 회원가입 처리 버튼
            Button(
                onClick = {
                    // 비밀번호 일치 여부 확인
                    if (password != passwordConfirm) {
                        errorMessage =
                            "비밀번호가 일치하지 않습니다."
                        return@Button
                    }

                    scope.launch {
                        // 이메일 중복 확인
                        val existing =
                            db.userDao()
                                .getUserByEmail(email.trim())

                        if (existing != null) {
                            errorMessage =
                                "이미 가입된 이메일입니다."
                        } else {
                            // 새로운 사용자 DB 저장
                            db.userDao().insert(
                                User(
                                    email = email.trim(),
                                    password = password
                                )
                            )

                            onSignUpComplete()
                        }
                    }
                },
                enabled = isFormFilled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonDark,
                    disabledContainerColor = ButtonDisabled
                )
            ) {
                Text(
                    text = "가입하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}