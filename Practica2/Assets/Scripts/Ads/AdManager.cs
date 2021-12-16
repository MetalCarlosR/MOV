using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AdManager : MonoBehaviour
{
    [SerializeField] InterstitialAd _interstitialAd;
    [SerializeField] RewardedAdsButton _rewardedAd;
    [SerializeField] BannerAd _bannerAd;

    // Start is called before the first frame update
    // is in start because the Advertisment intialization is in the awake of AdsInitializer
    void Start()
    {
        _interstitialAd.LoadAd();
        _rewardedAd.LoadAd();
        _bannerAd.LoadBanner();
    }

    public void ShowInterstitialAd()
    {
        _interstitialAd.ShowAd();
    }
}
