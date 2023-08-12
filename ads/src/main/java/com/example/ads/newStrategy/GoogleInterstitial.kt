package com.example.ads.newStrategy

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.Arrays
import java.util.Stack

//package com.vyroai.autocutcut.ads.max;
//
class GoogleInterstitial(context: Context?) {
    private val totalLevels = 4
    private var adUnits: ArrayList<ArrayList<Any>>? = null
    private val inter5 = "ca-app-pub-9507635869843997/4464793654"
    private val inter4 = "ca-app-pub-9507635869843997/2030202009"
    private val interHigh = "ca-app-pub-9507635869843997/9908692024"
    private val interMed = "ca-app-pub-9507635869843997/2524972129"
    private val interAll = "ca-app-pub-9507635869843997/5216316703"


    init {
        instantiateList()
        loadInitialInterstitials(context)
    }

    private fun instantiateList() {
        adUnits = ArrayList()

//        val testId = "ca-app-pub-3940256099942544/1033173712"
//
//        adUnits!!.add(0, ArrayList(listOf(testId, Stack<InterstitialAd>())))
//        adUnits!!.add(1, ArrayList(listOf(testId, Stack<InterstitialAd>())))
//        adUnits!!.add(2, ArrayList(listOf(testId, Stack<InterstitialAd>())))
//        adUnits!!.add(3, ArrayList(listOf(testId, Stack<InterstitialAd>())))
//        adUnits!!.add(4, ArrayList(listOf(testId, Stack<InterstitialAd>())))

        adUnits!!.add(0, ArrayList(listOf(inter5, Stack<InterstitialAd>())))
        adUnits!!.add(1, ArrayList(listOf(inter4, Stack<InterstitialAd>())))
        adUnits!!.add(2, ArrayList(listOf(interHigh, Stack<InterstitialAd>())))
        adUnits!!.add(3, ArrayList(listOf(interMed, Stack<InterstitialAd>())))
        adUnits!!.add(4, ArrayList(listOf(interAll, Stack<InterstitialAd>())))
    }

    fun loadInitialInterstitials(context: Context?) {
        InterstitialAdLoad(context, totalLevels)
    }

    fun getMediumAd(activity: Context?): InterstitialAd? {
        return getInterstitialAd(activity, 1)
    }

    fun getHighFloorAd(activity: Context?): InterstitialAd? {
        return getInterstitialAd(activity, 2)
    }

    fun getDefaultAd(activity: Context?): InterstitialAd? {
        return getInterstitialAd(activity, 0)
    }

    fun getInterstitialAd(activity: Context?, maxLevel: Int): InterstitialAd? {
        for (i in totalLevels downTo 0) {
            if (maxLevel > i) {
                break
            }
            val list = adUnits!![i]
            val adunitid = list[0] as String
            val stack = list[1] as Stack<InterstitialAd>
            InterstitialAdLoadSpecific(activity, adunitid, stack)
            if (stack != null && !stack.isEmpty()) {
                return stack.pop()
            }
        }
        return null
    }

    fun InterstitialAdLoad(activity: Context?, level: Int) {
        if (level < 0) {
            return
        }
        if (adUnits!!.size < level) {
            return
        }
        val list = adUnits!![level]
        val adunitid = list[0] as String
        val stack = list[1] as Stack<InterstitialAd>
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, adunitid, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                InterstitialAdLoad(activity, level - 1)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                stack.push(interstitialAd)
            }
        })
    }

    fun InterstitialAdLoadSpecific(
        activity: Context?,
        adUnitId: String?,
        stack: Stack<InterstitialAd>?
    ) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, adUnitId, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                super.onAdLoaded(interstitialAd)
                stack!!.push(interstitialAd)
            }
        })
    }
}