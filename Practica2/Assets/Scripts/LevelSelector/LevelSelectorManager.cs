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

    /// <summary>
    /// Creates all the groups stored in the groups list.
    /// Delegates the setup of the groups to the PackGroupUI component.
    /// </summary>
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

    /// <summary>
    /// Hides the groups list and setup and shows the pages for the selected pack.
    /// Also calculates the optimum size of the pages tiles so the number won't be
    /// calculated repeatedly in each page, always giving priority to the width.
    /// </summary>
    /// <param name="pack">Pack to load</param>
    /// <param name="color">Color associated with the pack</param>
    public void LoadPackGrid(LevelPack pack, Color color)
    {
        levelSelectorGrid.SetActive(true);
        levelPackName.gameObject.SetActive(true);
        levelPackName.text = pack.name;
        levelPackName.color = color;
        levelSelectorList.SetActive(false);
        DataManager.PackData packData = DataManager.GetPackData(pack.name);
        
        
        // Calculate the tileSize
        float targetWidth = pagesGroup.rect.width;
        float targetHeigth = pagesGroup.rect.height;

        float tileWidth = (targetWidth * 0.85f) / (5 + 4 / 3);

        if (tileWidth * (6 + 5 / 3) > targetHeigth*0.85f)
            tileWidth = (targetHeigth * 0.85f) / (6 + 5 / 3);
        
        for(int i = 0; i < packData.pagesData.Count; i++ )
        {
            PageUI page = Instantiate(pagePrefab, pagesGroup).GetComponent<PageUI>();
            page.SetupPage(pack.pages[i].pageName,packData.pagesData[i],tileWidth, targetWidth,pack.locked);
            Instantiate(circleIndicatorPrefab, indicatorGroup);
        }
        gridScroll.SetupScroll(indicatorGroup.transform);
    }

    /// <summary>
    /// Callback for the back button. Changes depending on witch part is showing
    /// right now.
    /// If is the pack goes back to the group list, if is the group list it goes
    /// back to the main menu.
    /// </summary>
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
