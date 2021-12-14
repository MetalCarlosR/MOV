using System.Collections;
using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class LevelSelectorManager : MonoBehaviour
{
    [SerializeField] private List<PackGroup> groups;
    [SerializeField] private GameObject groupPrefab, pagePrefab, circleIndicatorPrefab , levelSelectorGrid, levelSelectorList;
    [SerializeField] private RectTransform packsGroup, scrollGroup, heightOffset, pagesGroup, indicatorGroup;
    [SerializeField] private TextMeshProUGUI levelPackName;

    void Start()
    {
        float groupSize = 0;
        foreach (PackGroup g in groups)
        {
            PackGroupUI groupUI = Instantiate(groupPrefab, packsGroup).GetComponent<PackGroupUI>();
            groupUI.CreateGroup(g);
            groupSize += 150 * (g.packs.Length + 1);
        }
        
        scrollGroup.offsetMin = new Vector2(0, (heightOffset.rect.height - (heightOffset.rect.height *0.05f)) - groupSize);
    }

    public void LoadPackGrid(LevelPack pack, Color color)
    {
        levelSelectorGrid.SetActive(true);
        levelPackName.gameObject.SetActive(true);
        levelPackName.text = pack.name;
        levelPackName.color = color;
        levelSelectorList.SetActive(false);
        string[] data = pack.file.ToString().Split('\n');
        for (int i = 0; i < pack.pages.Length; i++)
        {
            PageUI page = Instantiate(pagePrefab, pagesGroup).GetComponent<PageUI>();
            string[] pageData = new string[30];
            for (int j = 0; j < 30; j++)
            {
                pageData[j] = data[30 * i + j];
            }
            page.SetupPage(pageData,pack.pages[i], i * 30 + 1);
            Instantiate(circleIndicatorPrefab, indicatorGroup);
        }
    }

    public void Back()
    {
        if (levelSelectorList.gameObject.activeSelf)
            GameManager.Instance.LoadScene(0);
        else
        {
            foreach (Transform t in pagesGroup.transform )
            {
                Destroy(t.gameObject);
            }
            foreach (Transform t in indicatorGroup.transform )
            {
                Destroy(t.gameObject);
            }
            levelSelectorGrid.SetActive(false);
            levelPackName.gameObject.SetActive(false);
            levelSelectorList.SetActive(true);
        }
    }
    
}
