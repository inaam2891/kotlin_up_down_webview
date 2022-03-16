package com.example.webviewinkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.DateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
//	private var webView: WebView? = null
    private var webView: WebView? = null
	var sURL: String? = null
	var sFileName: String? = null
	var sUserAgent: String? = null
	private var mUploadMessage: ValueCallback<Uri?>? = null
	var uploadMessage: ValueCallback<Array<Uri>>? = null
	var url = "https://www.supremeconverter.com/"
	private val ValueCallback: Any? = null
	private val webSettings: WebSettings? = null
	private val DownloadImageURL: String? = null


	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		webView = findViewById(R.id.webview)
		webView!!.settings.javaScriptEnabled = true
		webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
		webView!!.addJavascriptInterface(JavaScriptInterface(this), "Android")
		webView!!.loadUrl(url)
		webView!!.settings.allowFileAccess = true

		//webView.setWebViewClient(new xWebViewClient());

//		webView!!.setDownloadListener({ url, userAgent, contentDisposition, mimeType, contentLength ->
//			val request = DownloadManager.Request(Uri.parse(url))
//			request.setMimeType(mimeType)
//			request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url))
//			request.addRequestHeader("User-Agent", userAgent)
//			request.setDescription("Downloading file...")
//			request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
//			request.allowScanningByMediaScanner()
//			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//			request.setDestinationInExternalFilesDir(this@MainActivity, Environment.DIRECTORY_DOWNLOADS, ".png")
//			val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//			dm.enqueue(request)
//			Toast.makeText(applicationContext, "Downloading File", Toast.LENGTH_LONG).show()
//		})




		//code for showing download image in notification bar
		webView!!.webViewClient = object : WebViewClient()
		{
			override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
				if (url.endsWith(".png")) {
					startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
					// if want to download pdf manually create AsyncTask here
					// and download file
					return true
				}
				return false
			}
		}
		webView!!.webChromeClient = object : WebChromeClient() {
			// For 3.0+ Devices (Start)
			// onActivityResult attached before constructor
			protected fun openFileChooser(uploadMsg: ValueCallback<*>?, acceptType: String?) {
				mUploadMessage = uploadMsg as ValueCallback<Uri?>?
				val i = Intent(Intent.ACTION_GET_CONTENT)
				i.addCategory(Intent.CATEGORY_OPENABLE)
				i.type = "image/*"
				startActivityForResult(Intent.createChooser(i, "File Browser"),
					FILECHOOSER_RESULTCODE)
			}

			inner class WebviewFragment : Fragment() {
				var browser: WebView? = null

				// invoke this method after set your WebViewClient and ChromeClient
				private fun browserSettings() {
					browser!!.settings.javaScriptEnabled = true
					browser!!.setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->

						browser!!.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(
								url))
					}
					browser!!.settings.setAppCachePath(this.activity!!.applicationContext.cacheDir.absolutePath)
					browser!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
					browser!!.settings.databaseEnabled = true
					browser!!.settings.domStorageEnabled = true
					browser!!.settings.useWideViewPort = true
					browser!!.settings.loadWithOverviewMode = true
					browser!!.addJavascriptInterface(JavaScriptInterface(
						context), "Android")
					browser!!.settings.pluginState = WebSettings.PluginState.ON
				}
			}

			// For Lollipop 5.0+ Devices
			override fun onShowFileChooser(
				mWebView: WebView,
				filePathCallback: ValueCallback<Array<Uri>>,
				fileChooserParams: FileChooserParams, ): Boolean {
				if (uploadMessage != null) {
					uploadMessage!!.onReceiveValue(null)
					uploadMessage = null
				}
				uploadMessage = filePathCallback
				var intent: Intent? = null
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					intent = fileChooserParams.createIntent()
				}
				try {
					startActivityForResult(intent, REQUEST_SELECT_FILE)
				}
				catch (e: ActivityNotFoundException) {
					uploadMessage = null
					return false
				}
				return true
			}

			//For Android 4.1 only
			protected fun openFileChooser(


				uploadMsg: ValueCallback<Uri?>?,
				acceptType: String?,
				capture: String?,
										 ) {
				mUploadMessage = uploadMsg
				val intent = Intent(Intent.ACTION_GET_CONTENT)
				intent.addCategory(Intent.CATEGORY_OPENABLE)
				intent.type = "image/*"
				startActivityForResult(Intent.createChooser(intent, "File Browser"),
					FILECHOOSER_RESULTCODE)
			}

			protected fun openFileChooser(uploadMsg: ValueCallback<Uri?>?) {
				mUploadMessage = uploadMsg
				val i = Intent(Intent.ACTION_GET_CONTENT)
				i.addCategory(Intent.CATEGORY_OPENABLE)
				i.type = "image/*"
				startActivityForResult(Intent.createChooser(i, "File Chooser"),
					FILECHOOSER_RESULTCODE)
			}
		}
		webView!!.setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
			val fileName = URLUtil.guessFileName(url, contentDisposition, getFileType(url))
			sFileName = fileName
			sURL = url
			sUserAgent = userAgent
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (ContextCompat.checkSelfPermission(this@MainActivity,
						Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED
				) {
					downloadFile(sFileName, sURL, sUserAgent)
				}
				else {
					requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
						1001)
				}
			}
			else {
				downloadFile(sFileName, sURL, sUserAgent)
			}
		}
	}

	private fun downloadFile(fileName: String?, url: String?, userAgent: String?) {
		var url = url
		try {
			url = "https://www.supremeconverter.com/"
			// DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
			val request = DownloadManager.Request(Uri.parse(url))
			val cookie = CookieManager.getInstance().getCookie(url)
			request.setTitle(fileName)
				.setDescription("is being downloaded")
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)

				.addRequestHeader("cookie", cookie)
				.addRequestHeader("User-Agent", userAgent)
				.setMimeType(getFileType(url))
				.setAllowedOverMetered(true)
				.setAllowedOverRoaming(true)
				.setVisibleInDownloadsUi(true)
				.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE
						or DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
			request.setDestinationInExternalPublicDir(
				Environment.DIRECTORY_DOWNLOADS,  //Download folder
				URLUtil.guessFileName(DownloadImageURL, null, null)) //Name of file
			val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
			downloadManager.enqueue(request)
			sURL = ""
			sUserAgent = ""
			sFileName = ""
			Toast.makeText(this, "Download Started", Toast.LENGTH_LONG).show()
		}
		catch (ignored: Exception) {
			Toast.makeText(this, "error$ignored", Toast.LENGTH_SHORT).show()
			Log.d("@@@@", ignored.message!!)
		}
	}

	fun getFileType(url: String?): String? {
		val contentResolver = contentResolver
		val mimeTypeMap = MimeTypeMap.getSingleton()
		return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(Uri.parse(url)))
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<String>,
		grantResults: IntArray,
										   ) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 1001) {
			if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (sURL != "" && sFileName != "" && sUserAgent != "") {
					downloadFile(sFileName, sURL, sUserAgent)
				}
			}
		}
	}

	class JavaScriptInterface(private val context: Context?) {
		private val nm: NotificationManager? = null
		private val bitMap: Bitmap? = null

		@JavascriptInterface
		fun bitMapToBase64(): String {
			val byteArrayOutputStream = ByteArrayOutputStream()
			//add support for jpg and more.
			 bitMap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
			val byteArray = byteArrayOutputStream.toByteArray()
			return Base64.encodeToString(byteArray, Base64.DEFAULT)
		}
		@JavascriptInterface
		@Throws(IOException::class)
		fun getBase64FromBlobData(base64Data: String) {
			convertBase64StringToPdfAndStoreIt(base64Data)
		}
		@Throws(IOException::class)
		private fun convertBase64StringToPdfAndStoreIt(base64PDf: String) {
			Log.e("base64PDf", base64PDf)
			val currentDateTime = DateFormat.getDateTimeInstance().format(Date())
			val calendar = Calendar.getInstance()
			val dwldsPath = File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS)
				.toString() + "/Report" + calendar.timeInMillis + "_.xlsx")
			val pdfAsBytes =
				Base64.decode(base64PDf.replaceFirst("data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,".toRegex(),
					""), 0)
			Log.e("bytearray", "" + pdfAsBytes)
			val os: FileOutputStream
			os = FileOutputStream(dwldsPath, false)
			os.write(pdfAsBytes)
			os.flush()
			os.close()
			if (dwldsPath.exists()) {
				sendNotification()
				val dir = File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_DOWNLOADS).toString() + "/Report" +
						calendar.timeInMillis + "_.xlsx")
				val sendIntent = Intent(Intent.ACTION_VIEW)
				val path = dir.absolutePath
				val uri: Uri
				uri = if (Build.VERSION.SDK_INT < 24) {
					Uri.fromFile(dir)
				}
				else {
					val file = File(path)
					FileProvider.getUriForFile((context as MainActivity?)!!,
						context!!.applicationContext.packageName + ".provider", file)
					//                    uri = Uri.parse("file://" + dir);
				}
				sendIntent.setDataAndType(uri, "application/vnd.ms-excel")
				sendIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
				sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				try {
					context!!.startActivity(sendIntent)
				}
				catch (e: Exception) {
					Toast.makeText(context, "Np app found to view file", Toast.LENGTH_SHORT).show()
				}
			}
		}

		private fun scanFile(path: String) {
			MediaScannerConnection.scanFile(context, arrayOf(path), null
										   ) { path, uri ->
				Log.d("Tag",
					"Scan finished. You can view the image in the gallery now.")
			}
		}
		private fun sendNotification() {}
		companion object {
			fun decodeBase64(str: String?): Bitmap {
//            bye[] decodedByte = Base64.decode(input, Base64.DEFAULT);
//            return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
				val decodedByte = Base64.decode(str, Base64.DEFAULT)
				return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)


//            image.setImageBitmap(decodedByte);
			}

			fun getBase64StringFromBlobUrl(blobUrl: String): String {
				return if (blobUrl.startsWith("blob")) {
					"javascript: var xhr=new XMLHttpRequest();" +
							"xhr.open('GET', '" + blobUrl + "', true);" +
							"xhr.setRequestHeader('Content-type','application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8');" +
							"xhr.responseType = 'blob';" +
							"xhr.onload = function(e) {" +
							"    if (this.status == 200) {" +
							"        var blobPdf = this.response;" +
							"        var reader = new FileReader();" +
							"        reader.readAsDataURL(blobPdf);" +
							"        reader.unloaded = function() {" +
							"            base64data = reader.result;" +
							"            Android.getBase64FromBlobData(base64data);" +
							"        }" +
							"    }" +
							"};" +
							"xhr.send();"
				}
				else "javascript: console.log('It is not a Blob URL');"
			}
		}
	}

	public override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		super.onActivityResult(requestCode, resultCode, intent)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			if (requestCode == REQUEST_SELECT_FILE) {
				if (uploadMessage == null) return
				uploadMessage!!.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
				uploadMessage = null
			}
		}
		else if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage) return
			// Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
			// Use RESULT_OK only if you're implementing WebView inside an Activity
			val result = if (intent == null || resultCode != RESULT_OK) null else intent.data
			mUploadMessage!!.onReceiveValue(result)
			mUploadMessage = null
		}
	}


	 class xWebViewClient : WebViewClient() {
		override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
			view.loadUrl(url)
			return true
		}
	}

	override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
		if (keyCode == KeyEvent.KEYCODE_BACK && webView!!.canGoBack()) {
			webView!!.goBack()
			return true
		}
		return super.onKeyDown(keyCode, event)
	}

	companion object {
		const val REQUEST_SELECT_FILE = 100
		private const val FILECHOOSER_RESULTCODE = 1
	}
}

