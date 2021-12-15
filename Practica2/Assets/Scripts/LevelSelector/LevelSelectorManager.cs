using System.Collections.Generic;
using TMPro;
using UnityEngine;

public class LevelSelectorManager : MonoBehaviour
{
    [SerializeField] private List<PackGroup> groups;
    [SerializeField] private GameObject groupPrefab, pagePrefab, circleIndicatorPrefab , levelSelectorGrid, levelSelectorList;
    [SerializeField] private RectTransform packsGroup, scrollGroup, heightOffset, pagesGroup, indicatorGroup;
    [SerializeField] private TextMeshProUGUI levelPackName;
    [SerializeField] private GridScroll gridScroll;

    void Start()
    {
        float groupSize = 0;
        DataManager.LoadSaveData(groups);
        foreach (PackGroup g in groups)
        {
            PackGroupUI groupUI = Instantiate(groupPrefab, packsGroup).GetComponent<PackGroupUI>();
            groupUI.CreateGroup(g);
            groupSize += 150 * (g.packs.Length + 1);
        }
        
        scrollGroup.offsetMin = new Vector2(0, (heightOffset.rect.height*0.95f ) - groupSize);
    }

    public void LoadPackGrid(LevelPack pack, Color color)
    {
        levelSelectorGrid.SetActive(true);
        levelPackName.gameObject.SetActive(true);
        levelPackName.text = pack.name;
        levelPackName.color = color;
        levelSelectorList.SetActive(false);
        DataManager.PackData packData = DataManager.GetPackData(pack.name);
        for(int i = 0; i < packData.pagesData.Count; i++ )
        {
            PageUI page = Instantiate(pagePrefab, pagesGroup).GetComponent<PageUI>();
            page.SetupPage(pack.pages[i].pageName,packData.pagesData[i], pack.locked);
            Instantiate(circleIndicatorPrefab, indicatorGroup);
        }
        gridScroll.SetupScroll(indicatorGroup.transform);
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
