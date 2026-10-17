package com.example.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.R
import com.example.ui.theme.ThoshanAssistantTheme
import kotlinx.coroutines.delay

/**
 * Splash screen with smooth transitions and app branding.
 * Shows the Lotus Mark logo with app name, then transitions to MainActivity.
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            ThoshanAssistantTheme {
                SplashScreen()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreen() {
    var showContent by remember { mutableStateOf(false) }
    var navigateToMain by remember { mutableStateOf(false) }
    
    // Alpha animation for fade-in
    val alphaAnim = animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "splashAlpha"
    )
    
    // Scale animation for logo
    val scaleAnim = animateFloatAsState(
        targetValue = if (showContent) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "splashScale"
    )
    
    LaunchedEffect(Unit) {
        // Start animations
        showContent = true
        
        // Wait for animations to complete, then navigate
        delay(2000)
        navigateToMain = true
    }
    
    if (navigateToMain) {
        val intent = Intent(this@SplashScreen, MainActivity::class.java)
        startActivity(intent)
        finish()
    } else {
        // Splash screen content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lotus Mark Logo
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Thoshan Assistant",
                modifier = Modifier
                    .size(120.dp)
                    .alpha(alphaAnim.value)
                    .then(Modifier.scale(scaleAnim.value))
            )
            
            // Spacing
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.size(24.dp)
            )
            
            // App Name
            AnimatedContent(
                targetState = showContent,
                transitionSpec = {
                    if (targetState) {
                        (fadeIn(animationSpec = tween(800)) + scaleIn(animationSpec = tween(800)))
                            .togetherWith(fadeOut(animationSpec = tween(300)))
                    } else {
                        fadeOut(animationSpec = tween(300))
                    }.using(ContentTransform.copy(targetContentZIndex = 1f))
                },
                label = "appNameAnimation"
            ) { _ ->
                Text(
                    text = "Thoshan",
                    color = Color(0xFFE8703A), // Orange accent
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            
            // Subtitle
            if (showContent) {
                Text(
                    text = "Portfolio Assistant",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(alphaAnim.value)
                )
            }
        }
    }
}

// Preview for Android Studio
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen()
    }
}
