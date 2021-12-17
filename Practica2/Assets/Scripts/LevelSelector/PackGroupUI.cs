using TMPro;
using UnityEngine;
using UnityEngine.UI;

public class PackGroupUI : MonoBehaviour
{
    [SerializeField] private GameObject levelPackPrefab;

    [SerializeField] private RawImage bar, lowerBar;

    [SerializeField] private TextMeshProUGUI packText;

    [SerializeField] private RectTransform _rectTransform;

    /// <summary>
    /// Creates the group and instantiates all the packs for setup.
    /// Also scales the transform to fit the children.
    /// </summary>
    /// <param name="group">Data from the group to create</param>
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
            packUI.SetupPack(pack, group.color);
        }
    }
}