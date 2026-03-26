package com.ijs.user.presentation

import androidx.compose.runtime.Composable
import com.ijs.user.presentation.changepassword.ChangePasswordScreen
import com.ijs.user.presentation.changepassword.ChangePasswordViewModel
import com.ijs.user.presentation.forgotpassword.ForgotPasswordScreen
import com.ijs.user.presentation.forgotpassword.ForgotPasswordViewModel
import com.ijs.user.presentation.login.LoginScreen
import com.ijs.user.presentation.login.LoginViewModel
import com.ijs.user.presentation.profile.ProfileScreen
import com.ijs.user.presentation.profile.ProfileViewModel
import com.ijs.user.presentation.signup.SignUpScreen
import com.ijs.user.presentation.signup.SignUpViewModel

/**
 * Facade for the User feature module.
 *
 * Provides @Composable entry points for each user/auth screen.
 * The sharedUI module uses this facade to render user screens
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModels are passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
 */
object UserFeatureFacade {

    /**
     * Entry point for the Login screen.
     */
    @Composable
    fun LoginEntry(
        viewModel: LoginViewModel,
        onLoginSuccess: () -> Unit,
        onNavigateToSignUp: () -> Unit,
        onNavigateToForgotPassword: () -> Unit
    ) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = onLoginSuccess,
            onNavigateToSignUp = onNavigateToSignUp,
            onNavigateToForgotPassword = onNavigateToForgotPassword
        )
    }

    /**
     * Entry point for the Sign Up screen.
     */
    @Composable
    fun SignUpEntry(
        viewModel: SignUpViewModel,
        onSignUpSuccess: () -> Unit,
        onNavigateToLogin: () -> Unit
    ) {
        SignUpScreen(
            viewModel = viewModel,
            onSignUpSuccess = onSignUpSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }

    /**
     * Entry point for the Profile screen.
     */
    @Composable
    fun ProfileEntry(
        viewModel: ProfileViewModel,
        onNavigateToChangePassword: () -> Unit,
        onNavigateBack: () -> Unit,
        onLogout: () -> Unit
    ) {
        ProfileScreen(
            viewModel = viewModel,
            onNavigateToChangePassword = onNavigateToChangePassword,
            onNavigateBack = onNavigateBack,
            onLogout = onLogout
        )
    }

    /**
     * Entry point for the Change Password screen.
     */
    @Composable
    fun ChangePasswordEntry(
        viewModel: ChangePasswordViewModel,
        onNavigateBack: () -> Unit
    ) {
        ChangePasswordScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }

    /**
     * Entry point for the Forgot Password screen.
     */
    @Composable
    fun ForgotPasswordEntry(
        viewModel: ForgotPasswordViewModel,
        onNavigateToLogin: () -> Unit
    ) {
        ForgotPasswordScreen(
            viewModel = viewModel,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

