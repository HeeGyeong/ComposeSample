package com.example.composesample.presentation.example.component.ui.layout.animation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R

/**
 * Shared Element Transition Example UI
 *
 * Jetpack Compose의 Shared Element Transitions를 사용한 다양한 화면 전환 예제를 시연합니다.
 */
@Composable
fun SharedElementTransitionExampleUI(
    onBackEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        HeaderCard(onBackEvent = onBackEvent)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ImageExpansionExample() }
            item { ListToDetailExample() }
            item { TextTransformationExample() }
            item { MultipleElementsExample() }
        }
    }
}

@Composable
private fun HeaderCard(onBackEvent: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1976D2)
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "Shared Element Transitions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "화면 간 요소 전환 애니메이션",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImageExpansionExample() {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "1. Image Expansion",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "썸네일을 클릭하면 전체 화면으로 확대됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            SharedTransitionLayout {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "image_expansion"
                ) { expanded ->
                    if (expanded) {
                        ExpandedImageView(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onClose = { isExpanded = false }
                        )
                    } else {
                        ThumbnailImageView(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onClick = { isExpanded = true }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ThumbnailImageView(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .sharedBounds(
                    rememberSharedContentState(key = "image_container"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1976D2))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Sample Image",
                modifier = Modifier
                    .size(120.dp)
                    .sharedElement(
                        rememberSharedContentState(key = "image"),
                        animatedVisibilityScope = animatedContentScope
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpandedImageView(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClose: () -> Unit
) {
    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .sharedBounds(
                    rememberSharedContentState(key = "image_container"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1976D2))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Sample Image",
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "image"),
                        animatedVisibilityScope = animatedContentScope
                    ),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ListToDetailExample() {
    var selectedItem by remember { mutableIntStateOf(-1) }

    val items = remember {
        listOf(
            PhotoItem(1, "Mountain View", "Beautiful mountain landscape", Color(0xFF4CAF50)),
            PhotoItem(2, "Ocean Waves", "Peaceful ocean waves", Color(0xFF2196F3)),
            PhotoItem(3, "Forest Path", "Green forest trail", Color(0xFF8BC34A)),
            PhotoItem(4, "Desert Sunset", "Golden desert sunset", Color(0xFFFF9800)),
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "2. List to Detail",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "아이템마다 다른 애니메이션으로 전환됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            SharedTransitionLayout {
                AnimatedContent(
                    targetState = selectedItem,
                    transitionSpec = {
                        // 아이템 ID에 따라 다른 애니메이션 적용
                        val enterTransition = when (targetState) {
                            1 -> fadeIn(tween(300)) + slideInVertically(tween(300)) { -it / 2 }  // 위에서 아래로
                            2 -> fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it }    // 왼쪽에서 오른쪽으로
                            3 -> fadeIn(tween(300)) + slideInVertically(tween(300)) { it }       // 아래에서 위로
                            4 -> fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.8f)   // 스케일 + 페이드
                            else -> fadeIn(tween(300))
                        }
                        val exitTransition = when (initialState) {
                            1 -> fadeOut(tween(200)) + slideOutVertically(tween(200)) { it / 2 }
                            2 -> fadeOut(tween(200)) + slideOutHorizontally(tween(200)) { it }
                            3 -> fadeOut(tween(200)) + slideOutVertically(tween(200)) { -it }
                            4 -> fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.8f)
                            else -> fadeOut(tween(200))
                        }
                        enterTransition togetherWith exitTransition
                    },
                    label = "list_detail"
                ) { selected ->
                    if (selected >= 0) {
                        val item = items.find { it.id == selected }
                        if (item != null) {
                            DetailView(
                                item = item,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedContentScope = this@AnimatedContent,
                                onBack = { selectedItem = -1 }
                            )
                        }
                    } else {
                        ListView(
                            items = items,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onItemClick = { selectedItem = it.id }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ListView(
    items: List<PhotoItem>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onItemClick: (PhotoItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ListItemCard(
                item = item,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onClick = { onItemClick(item) }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ListItemCard(
    item: PhotoItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    rememberSharedContentState(key = "card_${item.id}"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                )
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "image_${item.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .clip(CircleShape)
                        .background(item.color)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "title_${item.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                    )
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailView(
    item: PhotoItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .sharedBounds(
                    rememberSharedContentState(key = "card_${item.id}"),
                    animatedVisibilityScope = animatedContentScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    }
                ),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "image_${item.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(item.color)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = item.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "title_${item.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.description,
                    fontSize = 16.sp,
                    color = Color(0xFF757575),
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("뒤로 가기")
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun TextTransformationExample() {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "3. Text Transformation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "텍스트를 클릭하면 크기와 위치가 변환됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            SharedTransitionLayout {
                AnimatedContent(
                    targetState = isExpanded,
                    transitionSpec = {
                        fadeIn(tween(400)) togetherWith fadeOut(tween(300))
                    },
                    label = "text_transform"
                ) { expanded ->
                    if (expanded) {
                        ExpandedTextView(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onClick = { isExpanded = false }
                        )
                    } else {
                        CollapsedTextView(
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onClick = { isExpanded = true }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CollapsedTextView(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Jetpack Compose",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "text_title"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "클릭하여 확장",
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ExpandedTextView(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Jetpack Compose",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6F00),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "text_title"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Modern toolkit for building native Android UI",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun MultipleElementsExample() {
    var selectedProfile by remember { mutableStateOf<ProfileItem?>(null) }

    val profiles = remember {
        listOf(
            ProfileItem(1, "Alice Johnson", "UX Designer", Color(0xFFE91E63)),
            ProfileItem(2, "Bob Smith", "Developer", Color(0xFF9C27B0)),
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "4. Multiple Elements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Text(
                text = "여러 요소가 동시에 전환됩니다.",
                fontSize = 14.sp,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(16.dp))

            SharedTransitionLayout {
                AnimatedContent(
                    targetState = selectedProfile,
                    transitionSpec = {
                        (fadeIn(tween(400)) + slideInVertically(tween(400)) { it })
                            .togetherWith(fadeOut(tween(300)) + slideOutVertically(tween(300)) { -it })
                    },
                    label = "profile"
                ) { profile ->
                    if (profile != null) {
                        ProfileDetailView(
                            profile = profile,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onBack = { selectedProfile = null }
                        )
                    } else {
                        ProfileListView(
                            profiles = profiles,
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animatedContentScope = this@AnimatedContent,
                            onProfileClick = { selectedProfile = it }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ProfileListView(
    profiles: List<ProfileItem>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onProfileClick: (ProfileItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        profiles.forEach { profile ->
            with(sharedTransitionScope) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick(profile) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .sharedElement(
                                    rememberSharedContentState(key = "avatar_${profile.id}"),
                                    animatedVisibilityScope = animatedContentScope
                                )
                                .clip(CircleShape)
                                .background(profile.color)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121),
                                modifier = Modifier.sharedElement(
                                    rememberSharedContentState(key = "name_${profile.id}"),
                                    animatedVisibilityScope = animatedContentScope
                                )
                            )
                            Text(
                                text = profile.role,
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                modifier = Modifier.sharedElement(
                                    rememberSharedContentState(key = "role_${profile.id}"),
                                    animatedVisibilityScope = animatedContentScope
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ProfileDetailView(
    profile: ProfileItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit
) {
    with(sharedTransitionScope) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .sharedElement(
                            rememberSharedContentState(key = "avatar_${profile.id}"),
                            animatedVisibilityScope = animatedContentScope
                        )
                        .clip(CircleShape)
                        .background(profile.color)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = profile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "name_${profile.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile.role,
                    fontSize = 18.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "role_${profile.id}"),
                        animatedVisibilityScope = animatedContentScope
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "About: Passionate professional with extensive experience in ${profile.role.lowercase()} and creative problem solving.",
                    fontSize = 14.sp,
                    color = Color(0xFF9E9E9E),
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("뒤로 가기")
                }
            }
        }
    }
}

data class PhotoItem(
    val id: Int,
    val title: String,
    val description: String,
    val color: Color
)

data class ProfileItem(
    val id: Int,
    val name: String,
    val role: String,
    val color: Color
)

@Preview(showBackground = true)
@Composable
fun SharedElementTransitionExampleUIPreview() {
    SharedElementTransitionExampleUI(onBackEvent = {})
}
