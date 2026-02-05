package com.nuda.nudaclient.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nuda.nudaclient.data.remote.dto.signup.SignupGetDraftResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SignupDataManager {

    // TODO: clearExpiredData(), isDraftExpired() 사용되는 액티비티에서 expiredAt 유효기간이 잘 전달되었는지 흐름 확인하기

    private const val PREF_NAME = "signup_data"
    private const val KEY_EXPIRES_AT = "expires_at"

    private const val KEY_NICKNAME = "nickname"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_PASSWORD_CHECK = "password_check"

    private const val KEY_RECIPIENT = "recipient"
    private const val KEY_PHONE_NUM = "phone_num"
    private const val KEY_POSTAL_CODE = "postal_code"
    private const val KEY_ADDRESS1 = "address1"
    private const val KEY_ADDRESS2 = "address2"

    private const val KEY_IRRITATING_LEVEL = "irritation_level"
    private const val KEY_CHANGE_FREQUENCY = "change_frequency"
    private const val KEY_THICKNESS = "thickness"
    private const val KEY_SCENT = "scent"
    private const val KEY_PRIORITY = "priority"
    private const val KEY_PRODUCT_IDS = "product_ids"

    private const val KEY_IS_NICKNAME_VALID = "is_nickname_valid"
    private const val KEY_IS_USERNAME_VALID = "is_username_valid"
    private const val KEY_IS_PW_VALID = "is_pw_valid"
    private const val KEY_IS_PW_CHECK_VALID = "is_pw_check_valid"
    private const val KEY_IS_EMAIL_VALID = "is_email_valid"
    private const val KEY_IS_EMAIL_CERTIFY_VALID = "is_email_certify_valid"
    private const val KEY_IS_NICKNAME_AVAILABLE = "is_nickname_available"
    private const val KEY_IS_USERNAME_AVAILABLE = "is_username_available"
    private const val KEY_IS_EMAIL_SEND_SUCCESS = "is_email_send_success"
    private const val KEY_IS_EMAIL_VERIFIED = "is_email_verified"

    private const val KEY_IS_RECIPIENT_VALID = "is_recipient_valid"
    private const val KEY_IS_PHONE_NUM_VALID = "is_phone_num_valid"
    private const val KEY_IS_ADDRESS2_VALID = "is_address2_valid"
    private const val KEY_IS_ADDRESS_FIND_SUCCESS = "is_address_find_success"


    private val gson = Gson()

    // object 변수 (메모리)
    // 회원가입 데이터
    var expiresAt: String? = null

    var nickname: String? = null
    var username: String? = null
    var email: String? = null
    var password: String? = null
    var passwordCheck: String? = null

    var recipient: String? = null
    var phoneNum: String? = null
    var postalCode: String? = null
    var address1: String? = null
    var address2: String? = null

    var irritationLevel: String? = null
    var changeFrequency: String? = null
    var thickness: String? = null
    var scent: String? = null
    var priority: String? = null
    var productIds: List<Int> = emptyList()

    // 유효성 검사 상태 데이터
    // 계정 정보
    var isNicknameValid = false
    var isUsernameValid = false
    var isPwValid = false
    var isPwCheckValid = false
    var isEmailValid = false
    var isEmailCertifyValid = false
    var isNicknameAvailable = false
    var isUsernameAvailable = false
    var isEmailSendSuccess = false
    var isEmailVerified = false
    // 배송 정보
    var isRecipientValid = false
    var isPhoneNumValid = false
    var isAddress2Valid = false
    var isAddressFindSuccess = false

    // 전체 Draft 데이터 저장 및 백업
    fun backupPrefData(context: Context) {
        // productIds 변환 (kotlin -> json)
        val productIdsJson = gson.toJson(productIds) // [1, 2, 3] -> "[1, 2, 3]"

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                // 만료 기간 저장
                putString(KEY_EXPIRES_AT, expiresAt)
                // 계정 정보 저장
                putString(KEY_NICKNAME, nickname)
                putString(KEY_USERNAME, username)
                putString(KEY_EMAIL, email)
                putString(KEY_PASSWORD, password)
                putString(KEY_PASSWORD_CHECK, passwordCheck)
                // 배송 정보 저장
                putString(KEY_RECIPIENT, recipient)
                putString(KEY_PHONE_NUM, phoneNum)
                putString(KEY_POSTAL_CODE, postalCode)
                putString(KEY_ADDRESS1, address1)
                putString(KEY_ADDRESS2, address2)
                // 설문조사 정보 저장
                putString(KEY_IRRITATING_LEVEL, irritationLevel)
                putString(KEY_CHANGE_FREQUENCY, changeFrequency)
                putString(KEY_THICKNESS, thickness)
                putString(KEY_SCENT, scent)
                putString(KEY_PRIORITY, priority)
                putString(KEY_PRODUCT_IDS, productIdsJson)

                //계정 정보 상태 저장
                putBoolean(KEY_IS_NICKNAME_VALID, isNicknameValid)
                putBoolean(KEY_IS_USERNAME_VALID, isUsernameValid)
                putBoolean(KEY_IS_PW_VALID, isPwValid)
                putBoolean(KEY_IS_PW_CHECK_VALID, isPwCheckValid)
                putBoolean(KEY_IS_EMAIL_VALID, isEmailValid)
                putBoolean(KEY_IS_EMAIL_CERTIFY_VALID, isEmailCertifyValid)
                putBoolean(KEY_IS_NICKNAME_AVAILABLE, isNicknameAvailable)
                putBoolean(KEY_IS_USERNAME_AVAILABLE, isUsernameAvailable)
                putBoolean(KEY_IS_EMAIL_SEND_SUCCESS, isEmailSendSuccess)
                putBoolean(KEY_IS_EMAIL_VERIFIED, isEmailVerified)
                // 배송 정보 상태 저장
                putBoolean(KEY_IS_RECIPIENT_VALID, isRecipientValid)
                putBoolean(KEY_IS_PHONE_NUM_VALID, isPhoneNumValid)
                putBoolean(KEY_IS_ADDRESS2_VALID, isAddress2Valid)
                putBoolean(KEY_IS_ADDRESS_FIND_SUCCESS, isAddressFindSuccess)
            }
    }

    // 복원
    fun loadPrefData(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

        nickname = pref.getString(KEY_NICKNAME, null)
        username = pref.getString(KEY_USERNAME, null)
        email = pref.getString(KEY_EMAIL, null)
        password = pref.getString(KEY_PASSWORD, null)
        passwordCheck = pref.getString(KEY_PASSWORD_CHECK, null)

        recipient = pref.getString(KEY_RECIPIENT, null)
        phoneNum = pref.getString(KEY_PHONE_NUM, null)
        postalCode = pref.getString(KEY_POSTAL_CODE, null)
        address1 = pref.getString(KEY_ADDRESS1, null)
        address2 = pref.getString(KEY_ADDRESS2, null)

        irritationLevel = pref.getString(KEY_IRRITATING_LEVEL, null)
        changeFrequency = pref.getString(KEY_CHANGE_FREQUENCY, null)
        thickness = pref.getString(KEY_THICKNESS, null)
        scent = pref.getString(KEY_SCENT, null)
        priority = pref.getString(KEY_PRIORITY, null)

        // productIds 복원 (json -> kotlin)
        val productIdsJson = pref.getString(KEY_PRODUCT_IDS, null)
        productIds = if (productIdsJson != null) {
            gson.fromJson(productIdsJson, object : TypeToken<List<Int>>() {}.type)
        } else {
            emptyList()
        }

        isNicknameValid = pref.getBoolean(KEY_IS_NICKNAME_VALID, false)
        isUsernameValid = pref.getBoolean(KEY_IS_USERNAME_VALID, false)
        isPwValid = pref.getBoolean(KEY_IS_PW_VALID, false)
        isPwCheckValid = pref.getBoolean(KEY_IS_PW_CHECK_VALID, false)
        isEmailValid = pref.getBoolean(KEY_IS_EMAIL_VALID, false)
        isEmailCertifyValid = pref.getBoolean(KEY_IS_EMAIL_CERTIFY_VALID, false)
        isNicknameAvailable = pref.getBoolean(KEY_IS_NICKNAME_AVAILABLE, false)
        isUsernameAvailable = pref.getBoolean(KEY_IS_USERNAME_AVAILABLE, false)
        isEmailSendSuccess = pref.getBoolean(KEY_IS_EMAIL_SEND_SUCCESS, false)
        isEmailVerified = pref.getBoolean(KEY_IS_EMAIL_VERIFIED, false)

        isRecipientValid = pref.getBoolean(KEY_IS_RECIPIENT_VALID, false)
        isPhoneNumValid = pref.getBoolean(KEY_IS_PHONE_NUM_VALID, false)
        isAddress2Valid = pref.getBoolean(KEY_IS_ADDRESS2_VALID, false)
        isAddressFindSuccess = pref.getBoolean(KEY_IS_ADDRESS_FIND_SUCCESS, false)
    }

    // draft 만료 여부 확인
    fun isDraftExpired() : Boolean { // true는 만료
        // expiresAt 형식 : "2026.01.24 (토) 17:42"

        val expiresAtStr = expiresAt ?: return true // null인 경우 만료

        try {
            // 날짜 문자열 파싱
            val dateOnly = expiresAtStr.substringBefore(" (")
            val timePart = expiresAtStr.substringAfterLast(") ")
            val dateTimeStr = "$dateOnly $timePart"

            // 문자열 Date 객체로 파싱
            val format = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
            val expiresAtDate = format.parse(dateTimeStr)

            return expiresAtDate?.before(Date()) ?: true
        } catch (e: Exception) {
            e.printStackTrace()
            return true // 파싱 실패 시 만료로 간주
        }
    }

    // draft 만료 시 전체 데이터 삭제
    fun clearExpiredData(context: Context) {
        if (isDraftExpired()) {
            clearAllData(context)
        }
    }

    // 전체 데이터 초기화
    fun clearAllData(context: Context) {
        // Pref 삭제
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit { clear() }

        // 회원가입 토큰 삭제
        TokenManager.clearSignupToken(context)

        // Object 변수 초기화
        expiresAt = null

        nickname = null
        username = null
        email = null
        password = null
        passwordCheck = null

        recipient = null
        phoneNum = null
        postalCode = null
        address1 = null
        address2 = null

        irritationLevel = null
        changeFrequency = null
        thickness = null
        scent = null
        priority = null
        productIds = emptyList()

        isNicknameValid = false
        isUsernameValid = false
        isPwValid = false
        isPwCheckValid = false
        isEmailValid = false
        isEmailCertifyValid = false
        isNicknameAvailable = false
        isUsernameAvailable = false
        isEmailSendSuccess = false
        isEmailVerified = false

        isRecipientValid = false
        isPhoneNumValid = false
        isAddress2Valid = false
        isAddressFindSuccess = false
    }
}