using System;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

public class GridScroll : ScrollRect
{
    [SerializeField] private RectTransform _transform;
    private HorizontalLayoutGroup _group;
    private ScrollRect _scrollRect;
    private List<Image> _dots;
    private int _index = 0;
    private float _logicWidth;
    private bool _cooldown = false;
    [SerializeField] private Color selected, unselected;

    private void Awake()
    {
        _dots = new List<Image>();
        _group = _transform.GetComponent<HorizontalLayoutGroup>();
        _logicWidth = _transform.rect.width;
        _scrollRect = GetComponent<ScrollRect>();
    }
    
    /// <summary>
    /// Setups the parameters for the creation of the scroll environment.
    /// Instantiates the indicators(dots) that points the curent page.
    /// </summary>
    /// <param name="dotGroup">Parent where indicators will be instantiated</param>
    public void SetupScroll(Transform dotGroup)
    {
        _dots.Clear();
        _index = 0;
        _group.padding = new RectOffset();
        foreach (Transform dot in dotGroup)
        {
            Image i = dot.GetComponent<Image>();
            i.color = unselected;
            _dots.Add(i);
        }

        if (_dots.Count != 0)
            _dots[_index].color = selected;
        else
            Debug.LogWarning("No dots detected");
    }

    /// <summary>
    /// Callback given to the scroll component, controls the "snapping" of the groups
    /// when a certain threshold is passed.
    /// When said threshold is passed the parent object of the grids changes it padding
    /// to be centered on the next grid, it also moves all the object so the "tp" is seamless
    /// and the user don't see a sudden movement.
    /// After that it gets into a cooldown period util it reaches a near center point
    /// witch disables the interaction with the scroll from the user.
    /// </summary>
    public void OnScrollMoved()
    {
        float leftOffset = _transform.offsetMin.x;

        int dir = leftOffset > 0 ? -1 : 1;

        if (!_cooldown)
        {
            if (Math.Abs(leftOffset) > _logicWidth / 4)
            {
                if (_index + dir >= _dots.Count || _index + dir < 0)
                    return;

                _dots[_index].color = unselected;

                _index += dir;

                _dots[_index].color = selected;

                float posOffset = -_index * _logicWidth;
                float newLeftOffset = leftOffset + _logicWidth * dir;

                RectOffset padding = _group.padding;
                padding.left += (int) _logicWidth * -dir;
                _group.padding = padding;
                _transform.offsetMin = new Vector2(newLeftOffset, 0);
                _transform.offsetMax = new Vector2(newLeftOffset, 0);
                _scrollRect.Rebuild(CanvasUpdate.Layout);
                _cooldown = true;
                PointerEventData point = new PointerEventData(EventSystem.current);
                point.button = PointerEventData.InputButton.Left;
                base.OnEndDrag(point);
                LayoutRebuilder.MarkLayoutForRebuild(_transform);
            }
        }
        else if (Math.Abs(leftOffset) < _logicWidth / 4)
        {
            _cooldown = false;
        }
    }

    /// <summary>
    /// Callback from parent class with cooldown to prevent undesired movement
    /// </summary>
    /// <param name="eventData"></param>
    public override void OnBeginDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnBeginDrag(eventData);
    }
    
    /// <summary>
    /// Callback from parent class with cooldown to prevent undesired movement
    /// </summary>
    /// <param name="eventData"></param>
    public override void OnDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnDrag(eventData);
    }
    
    /// <summary>
    /// Callback from parent class with cooldown to prevent undesired movement
    /// </summary>
    /// <param name="eventData"></param>
    public override void OnEndDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnEndDrag(eventData);
    }
}