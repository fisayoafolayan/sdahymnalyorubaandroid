package com.sdahymnal.yoruba.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sdahymnal.yoruba.R
import com.sdahymnal.yoruba.data.Hymn
import com.sdahymnal.yoruba.data.HymnCategory
import com.sdahymnal.yoruba.data.HymnCategoryStore
import com.sdahymnal.yoruba.ui.components.BrandHeader

private fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    // WORSHIP
    "praise"         -> Icons.Outlined.VolunteerActivism  // raised hands praising
    "sunrise"        -> Icons.Outlined.WbSunny            // morning sun
    "evening"        -> Icons.Outlined.NightsStay          // moon and stars
    "music"          -> Icons.Outlined.MusicNote           // opening hymn
    "closing"        -> Icons.Outlined.WbTwilight          // sunset/closing

    // TRINITY
    "trinity"        -> Icons.Outlined.ChangeHistory       // three-sided symbol

    // GOD THE FATHER
    "love_god"       -> Icons.Outlined.FavoriteBorder      // heart outline
    "majesty"        -> Icons.Outlined.Brightness7         // radiant glory
    "nature"         -> Icons.Outlined.Park                // trees/creation
    "faithful"       -> Icons.Outlined.VerifiedUser        // righteousness seal
    "grace"          -> Icons.Outlined.AutoAwesome          // sparkles of grace

    // JESUS CHRIST
    "birth"          -> Icons.Outlined.CardGiftcard         // gift of Christ
    "ministry"       -> Icons.AutoMirrored.Outlined.DirectionsWalk // walking/serving
    "suffering"      -> Icons.Outlined.HeartBroken          // suffering heart
    "resurrection"   -> Icons.Outlined.KeyboardArrowUp      // rising up
    "priesthood"     -> Icons.Outlined.WorkspacePremium     // priestly crown
    "love_christ"    -> Icons.Outlined.Favorite             // His love
    "second_advent"  -> Icons.Outlined.CloudQueue           // coming in clouds
    "kingdom"        -> Icons.Outlined.Castle               // kingdom/throne
    "glory"          -> Icons.Outlined.StarRate             // shining glory

    // HOLY SPIRIT
    "spirit"         -> Icons.Outlined.Air                  // wind/breath

    // HOLY SCRIPTURES
    "scripture"      -> Icons.Outlined.AutoStories          // open book

    // GOSPEL
    "invitation"     -> Icons.Outlined.MailOutline          // gospel call
    "repentance"     -> Icons.Outlined.Replay               // turning back
    "forgiveness"    -> Icons.Outlined.Diversity1           // reconciliation
    "consecration"   -> Icons.Outlined.LocalFireDepartment  // sanctifying flame
    "baptism"        -> Icons.Outlined.WaterDrop            // baptismal water
    "salvation"      -> Icons.Outlined.Shield               // protection/redemption

    // CHRISTIAN CHURCH
    "community"      -> Icons.Outlined.Groups               // fellowship
    "mission"        -> Icons.Outlined.Campaign              // church mission
    "dedication"     -> Icons.Outlined.Church                // house of God
    "ordination"     -> Icons.Outlined.Badge                 // laying on of hands

    // DOCTRINES
    "sabbath"        -> Icons.Outlined.EventAvailable        // rest day
    "communion"      -> Icons.Outlined.DinnerDining          // Lord's Supper
    "law"            -> Icons.Outlined.Gavel                 // law and grace
    "eternal"        -> Icons.Outlined.AllInclusive          // eternal/infinity

    // EARLY ADVENT
    "early_advent"   -> Icons.Outlined.History               // early expectations

    // CHRISTIAN LIFE
    "our_love"       -> Icons.Outlined.MonitorHeart          // heartfelt love
    "joy"            -> Icons.Outlined.LightMode             // bright joy
    "hope"           -> Icons.Outlined.Flare                 // hope's light
    "prayer"         -> Icons.Outlined.SelfImprovement       // meditation/prayer
    "faith"          -> Icons.Outlined.Anchor                // anchor of faith
    "guidance"       -> Icons.Outlined.Explore               // compass guidance
    "thankful"       -> Icons.Outlined.ThumbUp               // harvest thanks
    "humility"       -> Icons.Outlined.Spa                   // gentle spirit
    "service"        -> Icons.Outlined.Handshake             // work of love
    "obedience"      -> Icons.Outlined.CheckCircle           // obedient check
    "watchful"       -> Icons.Outlined.Visibility            // watchful eye
    "warfare"        -> Icons.Outlined.Security              // spiritual armor
    "pilgrimage"     -> Icons.Outlined.Hiking                // holy journey

    // CHRISTIAN HOME
    "marriage"       -> Icons.Outlined.Celebration           // wedding celebration

    // OFFERING & PRAYER
    "offering"       -> Icons.Outlined.Redeem                // offering/giving

    // CHILDREN
    "children"       -> Icons.Outlined.ChildCare             // children's songs

    // MISCELLANEOUS
    "misc"           -> Icons.Outlined.Shuffle               // variety

    // CHOIR
    "choir"          -> Icons.AutoMirrored.Outlined.QueueMusic // choir music

    // INDIGENOUS
    "indigenous"     -> Icons.Outlined.Public                // cultural/global

    else             -> Icons.Outlined.LibraryMusic
}

@Composable
fun CategoriesScreen(
    hymns: List<Hymn>,
    favorites: Set<Int>,
    onCategoryClick: (HymnCategory) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val allCategoriesWithCount = remember(hymns) {
        HymnCategoryStore.categories.map { category ->
            category to HymnCategoryStore.hymnsIn(category, hymns).size
        }.filter { it.second > 0 }
    }

    val filteredCategories = if (searchQuery.isBlank()) {
        allCategoriesWithCount
    } else {
        val q = searchQuery.lowercase().trim()
        allCategoriesWithCount.filter {
            it.first.name.lowercase().contains(q) ||
                it.first.englishTitle.lowercase().contains(q)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = { BrandHeader() },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CategorySearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
            )

            Text(
                text = pluralStringResource(R.plurals.category_count, filteredCategories.size, filteredCategories.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )

            if (filteredCategories.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.GridView,
                        contentDescription = "No categories",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.no_categories_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.try_different_search),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = filteredCategories,
                    key = { it.first.id },
                ) { (category, count) ->
                    CategoryCard(
                        category = category,
                        count = count,
                        onClick = { onCategoryClick(category) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategorySearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 4.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.search_categories_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun CategoryCard(
    category: HymnCategory,
    count: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(
            imageVector = categoryIcon(category.icon),
            contentDescription = category.name,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 2,
        )
        Text(
            text = category.englishTitle,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            maxLines = 2,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = pluralStringResource(R.plurals.hymn_count, count, count),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}
