using System;
using UnityEngine;
using UnityEngine.UI;
using UnityEngine.Advertisements;

public class RewardedAdsButton : MonoBehaviour, IUnityAdsLoadListener, IUnityAdsShowListener
{
    [SerializeField] string _androidAdUnitId = "Rewarded_Android";
    [SerializeField] string _iOSAdUnitId = "Rewarded_iOS";
    string _adUnitId;

    [SerializeField] private Button showAdButton;

    bool buttonInteractable = false;

    // Unity Ads 4.0.0 calls the completed method many times
    // also, there is no API nor examples because is still 10 days old
    // so we track if in this showing the callback had already been called
    bool rewarded = false;

    void Awake()
    {
// Get the Ad Unit ID for the current platform:
        _adUnitId = null; // This will remain null for unsupported platforms
#if UNITY_IOS
		_adUnitId = _iOsAdUnitId;
#elif UNITY_ANDROID
        _adUnitId = _androidAdUnitId;
#endif
        //Disable button until ad is ready to show
        buttonInteractable = false;
    }

    private void Start()
    {
        Advertisement.Load(_adUnitId, this);
    }

    // If the ad successfully loads, add a listener to the button and enable it:
    public void OnUnityAdsAdLoaded(string adUnitId)
    {
        if (adUnitId.Equals(_adUnitId))
        {
            if (showAdButton != null)
            {
                // Enable the button for users to click:
                showAdButton.interactable = true;
            }

            buttonInteractable = true;
        }
    }

    // Implement a method to execute when the user clicks the button.
    public void ShowAd()
    {
        if (showAdButton != null)
            // Disable the button: 
            showAdButton.interactable = false;
        // Then show the ad:
        Advertisement.Show(_adUnitId, this);

        rewarded = false;
    }

    // Implement the Show Listener's OnUnityAdsShowComplete callback method to determine if the user gets a reward:
    public void OnUnityAdsShowComplete(string adUnitId, UnityAdsShowCompletionState showCompletionState)
    {
        if (adUnitId.Equals(_adUnitId) && showCompletionState.Equals(UnityAdsShowCompletionState.COMPLETED))
        {
            // Grant a reward.
            if (!rewarded)
            {
                GameManager.Instance.addClue();

                rewarded = true;
            }
            // Load another ad:
            Advertisement.Load(_adUnitId, this);
        }
    }

    // Implement Load and Show Listener error callbacks:
    public void OnUnityAdsFailedToLoad(string adUnitId, UnityAdsLoadError error, string message)
    {
        Debug.Log($"Error loading Ad Unit {adUnitId}: {error.ToString()} - {message}");
        // Use the error details to determine whether to try to load another ad.
    }

    public void OnUnityAdsShowFailure(string adUnitId, UnityAdsShowError error, string message)
    {
        Debug.Log($"Error showing Ad Unit {adUnitId}: {error.ToString()} - {message}");
        // Use the error details to determine whether to try to load another ad.
    }

    public void OnUnityAdsShowStart(string adUnitId)
    {
    }

    public void OnUnityAdsShowClick(string adUnitId)
    {
    }
    

    void OnDestroy()
    {
        // Clean up the button listeners:
        if (showAdButton != null)
            showAdButton.onClick.RemoveAllListeners();
    }
}