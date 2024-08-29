package com.example.composesample.presentation.example.component.intent

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.composesample.presentation.legacy.base.SetSystemUI

@ExperimentalAnimationApi
class PassingIntentDataActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val passingIntentData: Bundle? = intent.extras

        setContent {
            SetSystemUI()

            Column {
                Text(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .fillMaxWidth(),
                    text = "Intent Data ---",
                    textAlign = TextAlign.Center
                )


                if (passingIntentData != null) {
                    PrintTextComponent("----- Search Key-Value -----")
                    for (key in passingIntentData.keySet()) {
                        val value = passingIntentData.get(key)
                        Log.d("Intent Data", "Key: $key, Value: $value")

                        if (key == "BundleData") {
                            val bundleData = passingIntentData.getBundle(key)
                                ?.getParcelable<ParcelableDataClass>("ParcelableData")

                            PrintTextComponent("Key: $key, Value: $bundleData")
                        } else {
                            PrintTextComponent("Key: $key, Value: $value")
                        }
                    }


                    PrintTextComponent("----- get Key Data -----")
                    PrintTextComponent("BooleanData Value: ${passingIntentData.getBoolean("BooleanData")}")
                    PrintTextComponent("StringData Value: ${passingIntentData.getString("StringData")}")
                    PrintTextComponent("BundleData Value: ${passingIntentData.getBundle("BundleData")}")
                    PrintTextComponent("NumberData Value: ${passingIntentData.getInt("NumberData")}")
                }
            }
        }
    }
}

@Composable
fun PrintTextComponent(text: String) {
    Text(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .fillMaxWidth(),
        text = text,
        textAlign = TextAlign.Center
    )
}