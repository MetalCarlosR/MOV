using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LevelSelectorManager : MonoBehaviour
{
    [SerializeField] private List<PackGroup> groups;
    [SerializeField] private GameObject groupPrefab;
    [SerializeField] private RectTransform packsGroup, scrollGroup, heightOffset;

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
    
}
