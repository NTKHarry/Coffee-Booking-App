package com.example.mobilcoffeebookingappmidterm2025.ui.user

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilcoffeebookingappmidterm2025.R
import com.example.mobilcoffeebookingappmidterm2025.data.MainRepository
import com.example.mobilcoffeebookingappmidterm2025.ui.UIConfig
import com.example.mobilcoffeebookingappmidterm2025.ui.theme.Colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInfoScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit = {},
    userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory(MainRepository))
) {
    val fullName by userViewModel.fullName.collectAsState()
    val phone by userViewModel.phoneNumber.collectAsState()
    val email by userViewModel.email.collectAsState()
    val address by userViewModel.deliveryLocation.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.back_arrow),
                            contentDescription = "back",
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                modifier = Modifier.padding(horizontal = UIConfig.TOP_BAR_SIDE_PADDING),
            )
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        ProfileScreenContent(
            fullName = fullName,
            phone = phone,
            email = email,
            address = address,
            onFullNameChanged = { userViewModel.updateUserInfo(fullName = it) },
            onPhoneChanged = { userViewModel.updateUserInfo(phoneNumber = it) },
            onEmailChanged = { userViewModel.updateUserInfo(email = it) },
            onAddressChanged = { userViewModel.updateUserInfo(deliveryLocation = it) },
            onLogoutClick = {
                userViewModel.logout()
                onLogout()
            },
            modifier = screenModifier.padding(horizontal = UIConfig.SCREEN_SIDE_PADDING)
        )
    }
}

@Composable
fun ProfileScreenContent(
    fullName: String,
    phone: String,
    email: String,
    address: String,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onAddressChanged: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Avatar section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Coffee cup avatar (using latte.png as default avatar)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .size(96.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.latte),
                    contentDescription = "User Avatar",
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        
        // Editable fields
        ProfileField(
            iconDrawable = R.drawable.person,
            label = "Full name",
            content = fullName,
            onSubmit = onFullNameChanged,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words
        )
        ProfileField(
            iconDrawable = R.drawable.phone,
            label = "Phone number",
            content = phone,
            onSubmit = onPhoneChanged,
            keyboardType = KeyboardType.Phone
        )
        ProfileField(
            iconDrawable = R.drawable.mail,
            label = "Email",
            content = email,
            onSubmit = onEmailChanged,
            isReadOnly = false, // Email is now editable
            keyboardType = KeyboardType.Email
        )
        ProfileField(
            iconDrawable = R.drawable.location_profile,
            label = "Address",
            content = address,
            onSubmit = onAddressChanged,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDC3545) // Red color for logout
            )
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Logout",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Logout",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun ProfileField(
    @DrawableRes iconDrawable: Int,
    label: String,
    content: String,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    isReadOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var editingText by rememberSaveable { mutableStateOf("") }
    
    // Update editingText when content changes and we're NOT editing
    LaunchedEffect(content) {
        if (!isEditing) {
            editingText = content
        }
    }
    
    if (isEditing && !isReadOnly) {
        EditField(
            iconDrawable = iconDrawable,
            label = label,
            content = editingText,
            onChanged = { editingText = it },
            onSubmit = { 
                onSubmit(editingText)
                isEditing = false
            },
            modifier = modifier,
            keyboardType = keyboardType,
            capitalization = capitalization
        )
    } else {
        DisplayField(
            iconDrawable = iconDrawable,
            label = label,
            content = content,
            onEditClick = { 
                if (!isReadOnly) {
                    editingText = content
                    isEditing = true
                }
            },
            modifier = modifier,
            showEditButton = !isReadOnly
        )
    }
}

@Composable
private fun DisplayField(
    @DrawableRes iconDrawable: Int,
    label: String,
    content: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
    showEditButton: Boolean = true
) {
    BaseField(
        leftIconDrawable = iconDrawable,
        rightIconDrawable = if (showEditButton) R.drawable.edit else null,
        label = label,
        onRightIconClick = onEditClick,
        modifier = modifier
    ) {
        Text(
            text = content,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
        )
    }
}

@Composable
private fun EditField(
    @DrawableRes iconDrawable: Int,
    label: String,
    content: String,
    onChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    val focusRequester = remember { FocusRequester() }

    BaseField(
        leftIconDrawable = iconDrawable,
        rightIconDrawable = R.drawable.right_arrow,
        label = label,
        onRightIconClick = onSubmit,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp, 5.dp)
        ) {
            BasicTextField(
                value = content,
                onValueChange = onChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Done,
                    capitalization = capitalization
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                ),
                singleLine = true
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun BaseField(
    @DrawableRes leftIconDrawable: Int,
    @DrawableRes rightIconDrawable: Int?,
    label: String,
    onRightIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentUnit: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .padding(15.dp, 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp),
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .size(42.dp),
            ) {
                Icon(
                    painter = painterResource(id = leftIconDrawable),
                    contentDescription = null,
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Colors.onBackground2
                    )
                )
                contentUnit()
            }
        }
        if (rightIconDrawable != null) {
            IconButton(
                onClick = onRightIconClick,
            ) {
                Icon(
                    painter = painterResource(id = rightIconDrawable),
                    contentDescription = null,
                )
            }
        }
    }
}
