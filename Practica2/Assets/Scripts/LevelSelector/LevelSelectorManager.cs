using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LevelSelectorManager : MonoBehaviour
{
    [SerializeField] private List<PackGroup> groups;
    [SerializeField] private GameObject groupPrefab, pagePrefab, circleIndicatorPrefab , levelSelectorGrid, levelSelectorList;
    [SerializeField] private RectTransform packsGroup, scrollGroup, heightOffset, pagesGroup, indicatorGroup;

    void Start()
    {
        float groupSize = 0;
        foreach (PackGroup g in groups)
        {
            PackGroupUI groupUI = Instantiate(groupPrefab, packsGroup).GetComponent<PackGroupUI>();
            groupUI.CreateGroup(g);
            groupSize += 75 * (g.packs.Length + 1);
        }
        
        scrollGroup.offsetMin = new Vector2(0, (heightOffset.rect.height-75) - groupSize);
    }

    public void LoadPackGrid(LevelPack pack)
    {
        levelSelectorGrid.SetActive(true);
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
    
}
