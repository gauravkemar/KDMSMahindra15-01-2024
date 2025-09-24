package com.kemarport.kdmsmahindra.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.kemarport.kdmsmahindra.R
import com.kemarport.kdmsmahindra.databinding.ActivityDeviceLockBinding
import com.kemarport.mahindrakiosk.helper.Constants
import com.kemarport.mahindrakiosk.helper.Utils
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException


class DeviceLockActivity : AppCompatActivity(), View.OnClickListener {
    private var binding: ActivityDeviceLockBinding? = null
    var Private =
        "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMB47/LvVL4/yb3vyygl/GTY/T2RuEwcZURTx6HlDNJYRd9Qa7o+wbhlGeHxqMg1akZ6Y05S2aFZv0r8DOq3WDPumRxNn0MbDwrhc4++bagtDZoVykJNsTCban5RaHHlrtlO0geL3mI/9ayqJIpCSL0JJYvWbbenWn0xfswUJXC3AgMBAAECgYBU4MBILLz9TRoFdcrsgJvmST6cOTfB8L/DuwKNZXobBKyDh26KaoR5cbRRTIW3DL86J1zFvImWuwI96hs0Ivh04b+kK+RIxVDEUFXbmFl8CmArTDch14aps0H33iuq0k6D2pXPZeanZjrk6WbXJ4zqA28WTLUesGi/FQfKTgc6YQJBAPO8n2b29CrFH7RXxSlVB8dhr2jj2FesWWbB92Cerbw8ouHQB0xJ9P3CzexLIAeai1dI9jeAmb1mtPUsBAmBBAcCQQDKKATvTHSoppeucvGwGBm5WX0ZxtFKYp3OxQoSEjSjh9MDeEVTASmspkJ8qGrayzvjFW1vRrpbKQfzsv38weHRAkAFFnLdW1pNaj/3K73Z1wSKjOd0cQ0bB2X2VyYqxiUqQLnFrOn6FbEfeumBfS/1+Kvf7o31KqIK6hHs7DZJXIARAkEAiKB1fVnZ+mXlW/S5nf8b26Y8o6A4sSJnb2jCPqZpRyC0H8M4bzn49YPEqcWVulvjFL3VoYBW0OexRSZy/FH5EQJBANUARwZ0YaRVJPCRZ7WRKJOtjTXZfHlKToC1ubzu1TaGXKTiB3tQe0eob4EAq0XhGxLI0afd5Y0a/D9svhiZcuU="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        binding = DataBindingUtil.setContentView(this, R.layout.activity_device_lock)
        binding!!.listener=this
        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding!!.edKey.setShowSoftInputOnFocus(false)
        } else {
            try {
                val method = EditText::class.java.getMethod(
                    "setShowSoftInputOnFocus", *arrayOf<Class<*>?>(Boolean::class.javaPrimitiveType)
                )
                method.isAccessible = true
                method.invoke(binding!!.edKey, false)
            } catch (e: Exception) {
                // ignore
            }
        }

        GlobalScope.launch(Dispatchers.IO){
            val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            val manufacturer = android.os.Build.MANUFACTURER
            val model = android.os.Build.MODEL

            try {
                val bitmap = encodeAsBitmap("$androidId-$manufacturer-$model-${getString(R.string.app_name)}")
                Log.e("androidId","${bitmap}")
                withContext(Dispatchers.Main) {
                    binding?.tvDeviceId?.text = androidId
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Device ID", androidId)
                    clipboard.setPrimaryClip(clip)

                    binding?.ivQR?.setImageBitmap(bitmap) // Updating UI on the main thread
                }
            } catch (ex: WriterException) {
                ex.printStackTrace()
            }
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.tvDeviceId -> {
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val clipboard =
                    getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Device ID", androidId)

                clipboard.setPrimaryClip(clip)
                //Toast.makeText(this, "Device ID copied to clipboard", Toast.LENGTH_SHORT).show();
                Toasty.info(this, "Device ID copied to clipboard").show()
            }
            R.id.btnRegister->{
                if (binding!!.edKey.getText().toString() != "") {
                    val encryptedData: String = binding!!.edKey.getText().toString().trim { it <= ' ' }
                    try {
                        val data: String? = decrypt(
                            encryptedData,
                            Private
                        )
                        val splitResult = data!!.split("-")
                        val id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                        if (splitResult[0].equals(id,true) && splitResult[1].equals(getString(R.string.app_name),true)) {
                            Utils.setSharedPrefsBoolean(
                                this@DeviceLockActivity,
                                Constants.IS_REGISTERED,
                                true
                            )
                            Toasty.success(this, "Device Registered Successfully").show()
                            startActivity(Intent(this@DeviceLockActivity, SplashActivity::class.java))
                            finish()
                        } else {
                            //Toast.makeText(this, "Invalid Key", Toast.LENGTH_SHORT).show();
                            Toasty.error(this, "Key didn't matched").show()
                            binding!!.edKey.setText("")
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                        Toasty.error(this, "Invalid Key").show()
                    }
                } else {
                    Toasty.error(this, "Please Enter Key!").show()
                    //Toast.makeText(this, "Please Enter Key!", Toast.LENGTH_SHORT).show();
                }
            }
            R.id.btnClear->{
                binding?.apply { edKey.setText("") }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getPublicKey(base64PublicKey: String): PublicKey? {
        var publicKey: PublicKey? = null
        try {
            val keySpec =
                X509EncodedKeySpec(Base64.getDecoder().decode(base64PublicKey.toByteArray()))
            val keyFactory = KeyFactory.getInstance("RSA")
            publicKey = keyFactory.generatePublic(keySpec)
            return publicKey
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return publicKey
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun getPrivateKey(base64PrivateKey: String?): PrivateKey? {
        var privateKey: PrivateKey? = null
        val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey))
        var keyFactory: KeyFactory? = null
        try {
            keyFactory = KeyFactory.getInstance("RSA")
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        try {
            privateKey = keyFactory!!.generatePrivate(keySpec)
        } catch (e: InvalidKeySpecException) {
            e.printStackTrace()
        }
        return privateKey
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(
        NoSuchPaddingException::class,
        NoSuchAlgorithmException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        IllegalBlockSizeException::class,
        IOException::class
    )
    fun decrypt(data: ByteArray?, privateKey: PrivateKey?): String? {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val finalData = cipher.doFinal(data)
        return String(finalData)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Throws(
        IllegalBlockSizeException::class,
        InvalidKeyException::class,
        BadPaddingException::class,
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        IOException::class
    )
    fun decrypt(data: String?, base64PrivateKey: String?): String? {
        return decrypt(
            Base64.getDecoder().decode(data?.toByteArray()),
            getPrivateKey(base64PrivateKey)
        )
    }

    @Throws(WriterException::class)
    fun encodeAsBitmap(str: String?): Bitmap? {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 400, 400)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                pixels[y * w + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}