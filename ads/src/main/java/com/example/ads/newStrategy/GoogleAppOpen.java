package com.example.ads.newStrategy;


import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

public class GoogleAppOpen {

    final private int totalLevels = 4;
    private ArrayList<ArrayList<Object>> adUnits;

    private final String adUnitId = "ca-app-pub-9507635869843997/4847937031";

    private final String high = "ca-app-pub-9507635869843997/3164934674";

    private final String medium = "ca-app-pub-9507635869843997/2973362986";

    private final String one = "ca-app-pub-9507635869843997/9347199645";

    private final String two = "ca-app-pub-9507635869843997/9155627959";


    public GoogleAppOpen(Context context) {
        instantiateList();
        loadAppopenStart(context);
    }


    private void instantiateList() {
        adUnits = new ArrayList<>();

        adUnits.add(0, new ArrayList<Object>(Arrays.asList(high, new Stack<AppOpenAd>())));
        adUnits.add(1, new ArrayList<Object>(Arrays.asList(medium, new Stack<AppOpenAd>())));
        adUnits.add(2, new ArrayList<Object>(Arrays.asList(adUnitId, new Stack<AppOpenAd>())));
        adUnits.add(3, new ArrayList<Object>(Arrays.asList(one, new Stack<AppOpenAd>())));
        adUnits.add(4, new ArrayList<Object>(Arrays.asList(two, new Stack<AppOpenAd>())));
    }

    public void loadAppopenStart(Context context) {
        AppOpenAdLoad(context, totalLevels);
    }


    public AppOpenAd getAd(Context activity) {
        for (int i = totalLevels; i >= 0; i--) {
            ArrayList<Object> list = adUnits.get(i);
            String adunitid = (String) list.get(0);
            Stack<AppOpenAd> stack = (Stack<AppOpenAd>) list.get(1);
            AppOpenLoadSpecific(activity, adunitid, stack);
            if (stack != null && !stack.isEmpty()) {
                return stack.pop();
            }
        }

        return null;
    }

    public void AppOpenAdLoad(Context activity, int level) {
        if (level < 0) {
            return;
        }

        if (adUnits.size() < level) {
            return;
        }

        ArrayList<Object> list = adUnits.get(level);
        String adunitid = (String) list.get(0);
        Stack<AppOpenAd> stack = (Stack<AppOpenAd>) list.get(1);

        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(activity, adunitid, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                stack.push(ad);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                AppOpenAdLoad(activity, level - 1);
            }
        });
    }

    public void AppOpenLoadSpecific(Context activity, String adUnitId, Stack<AppOpenAd> stack) {
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(activity, adUnitId, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, new AppOpenAd.AppOpenAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull AppOpenAd ad) {
                stack.push(ad);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
            }
        });
    }

}
