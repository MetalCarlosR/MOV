using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class AdManager : MonoBehaviour
{
    [SerializeField] InterstitialAd _interstitialAd;
    [SerializeField] BannerAd _bannerAd;

    // Start is called before the first frame update
    // is in start because the Advertisment intialization is in the awake of AdsInitializer
    void Start()
    {
        _interstitialAd.LoadAd();
        _bannerAd.LoadBanner();
    }

    /// <summary>
    /// Show an interstitial ad
    /// </summary>
    public void ShowInterstitialAd()
    {
        _interstitialAd.ShowAd();
    }
}
