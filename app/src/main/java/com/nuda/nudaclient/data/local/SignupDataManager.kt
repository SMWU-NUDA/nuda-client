package com.nuda.nudaclient.data.local

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nuda.nudaclient.data.remote.dto.signup.SignupGetDraftResponse

object SignupDataManager {
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
    private const val KEY_IS_FORMATTING = "is_formatting"


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

    // 조회한 draft 데이터를 object 변수에 저장
    fun saveDraftToPref(data: SignupGetDraftResponse.Data) {
        if (data.accountInfo.nickname != null) {
            expiresAt = data.expiresAt
            nickname = data.accountInfo.nickname
            username = data.accountInfo.username
            email = data.accountInfo.email
        }

        if (data.deliveryInfo.recipient != null) {
            recipient = data.deliveryInfo.recipient
            phoneNum = data.deliveryInfo.phoneNum
            postalCode = data.deliveryInfo.postalCode
            address1 = data.deliveryInfo.address1
            address2 = data.deliveryInfo.address2
        }

        // 설문 데이터는 서버에 있을 때만 업데이트
        if (data.surveyInfo.irritationLevel != null) {
            irritationLevel = data.surveyInfo.irritationLevel
            changeFrequency = data.surveyInfo.changeFrequency
            thickness = data.surveyInfo.thickness
            scent = data.surveyInfo.scent
            priority = data.surveyInfo.priority
            productIds = data.surveyInfo.productIds
        }
    }

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

    // draft 만료 시 삭제

}