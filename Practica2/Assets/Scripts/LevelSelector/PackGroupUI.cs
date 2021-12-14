using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class PackGroupUI : MonoBehaviour
{
    [SerializeField] private GameObject levelPackPrefab;

    [SerializeField] private RawImage bar, lowerBar;

    [SerializeField] private TextMeshProUGUI packText;

    private RectTransform _rectTransform;

    
    private void Awake()
    {
        _rectTransform = GetComponent<RectTransform>();
    }

    public void CreateGroup(PackGroup group)
    {
        Color barColor = group.color;
        barColor.a = 0.4f;
        bar.color = barColor;
        lowerBar.color = group.color;
        packText.text = group.name;
        _rectTransform.sizeDelta = new Vector2(600, 150 * (group.packs.Length + 1));
        foreach (LevelPack pack in group.packs)
        {
            LevelPackUI packUI = Instantiate(levelPackPrefab, _rectTransform).GetComponent<LevelPackUI>();
            packUI.SetupPack(pack,group.color);
        }
    }
}