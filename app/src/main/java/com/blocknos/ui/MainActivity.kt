package com.blocknos.ui

import android.Manifest
import android.content.res.ColorStateList
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blocknos.R
import com.blocknos.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var numbersAdapter: BlockedItemAdapter
    private lateinit var prefixesAdapter: BlockedItemAdapter
    private lateinit var contactsAdapter: BlockedItemAdapter
    private lateinit var callLogAdapter: CallLogAdapter
    private lateinit var deviceCallLogAdapter: DeviceCallLogAdapter

    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS
    )

    private val contactPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val contactUri: Uri? = result.data?.data
                if (contactUri != null) {
                    val cursor = contentResolver.query(
                        contactUri,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER
                        ),
                        null, null, null
                    )
                    cursor?.use {
                        if (it.moveToFirst()) {
                            val name = it.getString(0)
                            val number = it.getString(1)
                            val e164 = PhoneNumberUtils.normalizeNumber(number)
                            if (viewModel.addBlockedContact(name, e164)) {
                                showSnackbar(getString(R.string.msg_contact_blocked, name ?: e164))
                            }
                        }
                    }
                }
            }
        }

    private val roleRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Call screening role granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Call screening role not granted", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupTabs()
        setupAdapters()
        setupClickListeners()
        observeData()

        requestNeededPermissions()
        requestCallScreeningRoleIfNeeded()
        viewModel.updateSummary()
        viewModel.loadDeviceCallLog()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            viewModel.loadDeviceCallLog()
        }
    }

    private fun setupTabs() {
        val tabButtons = listOf(
            binding.tabManualBlock,
            binding.tabCallLogBlock,
            binding.tabRuleHistory,
            binding.tabCallBlockHistory
        )

        tabButtons.forEachIndexed { index, button ->
            button.setOnClickListener { showTab(index) }
        }

        showTab(0)
    }

    private fun showTab(index: Int) {
        val showManualBlock = index == 0
        val showDeviceCallLog = index == 1
        val showBlockedRulesHistory = index == 2
        val showBlockedCallHistory = index == 3

        binding.cardBlockNumberInput.visibility = if (showManualBlock) View.VISIBLE else View.GONE
        binding.cardBlockPrefixInput.visibility = if (showManualBlock) View.VISIBLE else View.GONE
        binding.cardBlockContactInput.visibility = if (showManualBlock) View.VISIBLE else View.GONE

        binding.cardRecentCalls.visibility = if (showDeviceCallLog) View.VISIBLE else View.GONE

        binding.cardBlockedNumbers.visibility = if (showBlockedRulesHistory) View.VISIBLE else View.GONE
        binding.cardBlockedPrefixes.visibility = if (showBlockedRulesHistory) View.VISIBLE else View.GONE
        binding.cardBlockedContacts.visibility = if (showBlockedRulesHistory) View.VISIBLE else View.GONE

        binding.cardBlockedCallHistory.visibility = if (showBlockedCallHistory) View.VISIBLE else View.GONE

        updateTabSelection(index)

        if (showDeviceCallLog) {
            refreshDeviceCallLog()
        }
    }

    private fun updateTabSelection(selectedIndex: Int) {
        val tabs = listOf(
            binding.tabManualBlock,
            binding.tabCallLogBlock,
            binding.tabRuleHistory,
            binding.tabCallBlockHistory
        )

        val selectedBg = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_light))
        val defaultBg = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface))
        val stroke = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
        val text = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))

        tabs.forEachIndexed { index, button: MaterialButton ->
            button.backgroundTintList = if (index == selectedIndex) selectedBg else defaultBg
            button.strokeColor = stroke
            button.setTextColor(text)
        }
    }

    private fun setupAdapters() {
        numbersAdapter = BlockedItemAdapter { item ->
            viewModel.blockedNumbers.value?.find { it.id == item.id }?.let {
                viewModel.deleteBlockedNumber(it)
                showSnackbar(getString(R.string.msg_item_removed))
            }
        }
        prefixesAdapter = BlockedItemAdapter { item ->
            viewModel.blockedPrefixes.value?.find { it.id == item.id }?.let {
                viewModel.deleteBlockedPrefix(it)
                showSnackbar(getString(R.string.msg_item_removed))
            }
        }
        contactsAdapter = BlockedItemAdapter { item ->
            viewModel.blockedContacts.value?.find { it.id == item.id }?.let {
                viewModel.deleteBlockedContact(it)
                showSnackbar(getString(R.string.msg_item_removed))
            }
        }
        callLogAdapter = CallLogAdapter()

        deviceCallLogAdapter = DeviceCallLogAdapter { entry ->
            val normalized = PhoneNumberUtils.normalizeNumber(entry.number)
            if (viewModel.addBlockedNumber(normalized)) {
                showSnackbar(getString(R.string.msg_number_blocked, normalized))
            }
        }

        binding.recyclerBlockedNumbers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = numbersAdapter
        }
        binding.recyclerBlockedPrefixes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = prefixesAdapter
        }
        binding.recyclerBlockedContacts.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = contactsAdapter
        }
        binding.recyclerCallHistory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = callLogAdapter
        }
        binding.recyclerDeviceCallLog.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = deviceCallLogAdapter
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddNumber.setOnClickListener {
            val number = binding.editBlockedNumber.text.toString()
            val normalized = PhoneNumberUtils.normalizeNumber(number)
            if (viewModel.addBlockedNumber(normalized)) {
                showSnackbar(getString(R.string.msg_number_blocked, normalized))
                binding.editBlockedNumber.text?.clear()
            }
        }

        binding.buttonAddPrefix.setOnClickListener {
            val prefix = binding.editBlockedPrefix.text.toString()
            if (viewModel.addBlockedPrefix(prefix)) {
                showSnackbar(getString(R.string.msg_prefix_blocked, prefix))
                binding.editBlockedPrefix.text?.clear()
            }
        }

        binding.buttonAddContact.setOnClickListener {
            pickContact()
        }

        binding.checkUseSpamDb.setOnCheckedChangeListener { _, isChecked ->
            showSnackbar(
                if (isChecked) "Public spam database enabled" else "Public spam database disabled"
            )
        }

        binding.buttonClearHistory.setOnClickListener {
            viewModel.clearCallLog()
            showSnackbar(getString(R.string.msg_history_cleared))
        }

        binding.buttonRefreshCallLog.setOnClickListener {
            refreshDeviceCallLog(forceMessage = true)
        }
    }

    private fun refreshDeviceCallLog(forceMessage: Boolean = false) {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            viewModel.loadDeviceCallLog()
            if (forceMessage) {
                showSnackbar(getString(R.string.action_refresh))
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CALL_LOG), 100)
            showSnackbar(getString(R.string.msg_permission_needed))
        }
    }

    private fun observeData() {
        viewModel.summary.observe(this) { summary ->
            binding.textSummary.text = summary
        }

        viewModel.blockedNumbers.observe(this) { list ->
            val items = list.map { BlockedItem(it.id, it.phoneNumber) }
            numbersAdapter.submitList(items)
            binding.emptyBlockedNumbers.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerBlockedNumbers.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.blockedPrefixes.observe(this) { list ->
            val items = list.map { BlockedItem(it.id, it.prefix) }
            prefixesAdapter.submitList(items)
            binding.emptyBlockedPrefixes.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerBlockedPrefixes.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.blockedContacts.observe(this) { list ->
            val items = list.map { BlockedItem(it.id, it.contactName ?: it.phoneNumber, it.phoneNumber) }
            contactsAdapter.submitList(items)
            binding.emptyBlockedContacts.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerBlockedContacts.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.callLog.observe(this) { list ->
            callLogAdapter.submitList(list)
            binding.emptyCallHistory.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerCallHistory.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.deviceCallLog.observe(this) { list ->
            deviceCallLogAdapter.submitList(list)
            binding.emptyDeviceCallLog.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerDeviceCallLog.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun requestNeededPermissions() {
        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, notGranted.toTypedArray(), 100)
        }
    }

    private fun pickContact() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
        }
        contactPickerLauncher.launch(intent)
    }

    private fun requestCallScreeningRoleIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleRequestLauncher.launch(intent)
            }
        }
    }
}

