package com.example.ads.newStrategy;//package com.vyroai.autocutcut.ads.max;
//

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;
public class GoogleInterstitial {

    private final int totalLevels = 4;
    private ArrayList<ArrayList<Object>> adUnits;

    private final String adUnitId = "ca-app-pub-9507635869843997/9908714105";

    private final String high = "ca-app-pub-9507635869843997/9908692024";

    private final String medium = "ca-app-pub-9507635869843997/2524972129";

    private final String one = "ca-app-pub-9507635869843997/2030202009";

    private final String two = "ca-app-pub-9507635869843997/4464793654";

    public GoogleInterstitial(Context context) {
        instantiateList();
        loadInitialInterstitials(context);
    }

    private void instantiateList() {
        adUnits = new ArrayList<>();

        adUnits.add(0, new ArrayList<Object>(Arrays.asList(high, new Stack<AppOpenAd>())));
        adUnits.add(1, new ArrayList<Object>(Arrays.asList(medium, new Stack<AppOpenAd>())));
        adUnits.add(2, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<AppOpenAd>())));
        adUnits.add(3, new ArrayList<Object>(Arrays.asList(one, new Stack<AppOpenAd>())));
        adUnits.add(4, new ArrayList<Object>(Arrays.asList(two, new Stack<AppOpenAd>())));
    }

    public void loadInitialInterstitials(Context context) {
        InterstitialAdLoad(context, totalLevels);
    }


    public InterstitialAd getMediumAd(Context activity) {
        return getInterstitialAd(activity, 1);
    }

    public InterstitialAd getHighFloorAd(Context activity) {
        return getInterstitialAd(activity, 2);
    }

    public InterstitialAd getDefaultAd(Context activity) {
        return getInterstitialAd(activity, 0);
    }


    public InterstitialAd getInterstitialAd(Context activity, int maxLevel) {
        for (int i = totalLevels; i >= 0; i--) {

            if (maxLevel > i) {
                break;
            }

            ArrayList<Object> list = adUnits.get(i);
            String adunitid = (String) list.get(0);
            Stack<InterstitialAd> stack = (Stack<InterstitialAd>) list.get(1);

            InterstitialAdLoadSpecific(activity, adunitid, stack);

            if (stack != null && !stack.isEmpty()) {
                return stack.pop();
            }
        }

        return null;
    }

    public void InterstitialAdLoad(Context activity, int level) {

        if (level < 0) {
            return;
        }

        if (adUnits.size() < level) {
            return;
        }

        ArrayList<Object> list = adUnits.get(level);
        String adunitid = (String) list.get(0);
        Stack<InterstitialAd> stack = (Stack<InterstitialAd>) list.get(1);

        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adunitid, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                InterstitialAdLoad(activity, level - 1);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                stack.push(interstitialAd);
            }
        });
    }

    public void InterstitialAdLoadSpecific(Context activity, String adUnitId, Stack<InterstitialAd> stack) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adUnitId, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                super.onAdLoaded(interstitialAd);
                stack.push(interstitialAd);
            }
        });
    }
}

