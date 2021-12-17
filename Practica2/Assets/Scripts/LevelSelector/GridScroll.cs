using System;
using System.Collections;
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
    private int index = 0;
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

    
    public void SetupScroll(Transform dotGroup)
    {
        _dots.Clear();
        index = 0;
        _group.padding = new RectOffset();
        foreach (Transform dot in dotGroup)
        {
            Image i = dot.GetComponent<Image>();
            i.color = unselected;
            _dots.Add(i);
        }

        if (_dots.Count != 0)
            _dots[index].color = selected;
        else
            Debug.LogWarning("No dots detected");
    }

    public void OnScrollMoved()
    {
        float leftOffset = _transform.offsetMin.x;

        int dir = leftOffset > 0 ? -1 : 1;

        if (!_cooldown)
        {
            if (Math.Abs(leftOffset) > _logicWidth / 4)
            {
                if (index + dir >= _dots.Count || index + dir < 0)
                    return;

                _dots[index].color = unselected;

                index += dir;

                _dots[index].color = selected;

                float posOffset = -index * _logicWidth;
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

    public override void OnBeginDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnBeginDrag(eventData);
    }
    
    public override void OnDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnDrag(eventData);
    }
    
    public override void OnEndDrag(PointerEventData eventData)
    {
        if(_cooldown)
            return;
        base.OnEndDrag(eventData);
    }
}