using UnityEngine;

/**
 * This class will handle the visual representation of the flow in each cell
 */
public class Cell : MonoBehaviour
{
    [SerializeField]
    [Tooltip("Border of cell")]
    private SpriteRenderer border;

    [SerializeField]
    [Tooltip("Used as a beginning/end of flow and as last selected player position")]
    private SpriteRenderer circle;

    [SerializeField]
    [Tooltip("Shown when a clue is given for this cell")]
    private SpriteRenderer star;
    
    [SerializeField]
    [Tooltip("Background to fill with the flow's color")]
    private SpriteRenderer background;

    // Connections to be made by the flows
    [SerializeField]
    private SpriteRenderer upConnection;
    [SerializeField]
    private SpriteRenderer downConnection;
    [SerializeField]
    private SpriteRenderer leftConnection;
    [SerializeField]
    private SpriteRenderer rightConnection;

    // Walls to block the way of the flows
    [SerializeField]
    private SpriteRenderer upWall;
    [SerializeField]
    private SpriteRenderer downWall;
    [SerializeField]
    private SpriteRenderer leftWall;
    [SerializeField]
    private SpriteRenderer rightWall;

    private Color _color;
    
    // if its a starting/finnishing point for a flow
    private bool _isCircle = false;
    // if its a hole where in the grid
    private bool _isHole = false;
    // if its in use by a flow
    private bool _inUse = false;
    
    private int _x;
    private int _y;

    public enum WallsDirs
    {
        up = 0,
        down,
        left,
        right
    }

    // the walls status in this cell
    // used to check if there is a wall in some direction of this cell
    private bool[] _walls = { false, false, false, false };
    
    public bool IsCircle()
    {
        return _isCircle;
    }
    public bool IsHole()
    {
        return _isHole;
    }
    public bool IsInUse()
    {
        return _inUse;
        
    }
    public Color GetColor()
    {
        return _color;
    }

    public void SetColor(Color c)
    {
        circle.color = c;
        _color = c;
        rightConnection.color = c;
        leftConnection.color = c;
        upConnection.color = c;
        downConnection.color = c;
    }

    public void SetGridColor(Color c)
    {
        border.color = c;
    }

    public void ResetCell()
    {
        upConnection.enabled = false;
        downConnection.enabled = false;
        leftConnection.enabled = false;
        rightConnection.enabled = false;
        _inUse = false;
        background.enabled = false;
        if (!_isCircle)
            _color = Color.black;
        star.enabled = false;

        DespawnMiniCircle();
    }

    public void ResetCellNoBackground()
    {
        upConnection.enabled = false;
        downConnection.enabled = false;
        leftConnection.enabled = false;
        rightConnection.enabled = false;
        _inUse = false;
        if (!_isCircle)
            _color = Color.black;
        DespawnMiniCircle();
    }

    /// <summary>
    /// Sets the sprite to render a circle of the given color
    /// </summary>
    public void SetCircle(Color c)
    {
        circle.enabled = true;
        circle.color = c;
        _color = c;
        rightConnection.color = c;
        leftConnection.color = c;
        upConnection.color = c;
        downConnection.color = c;
        _isCircle = true;
    }

    #region Connections
    public void ConnectUp()
    {
        upConnection.enabled = true;
        _inUse = true;
    }

    public void DisconnectUp()
    {
        upConnection.enabled = false;
    }

    public void ConnectDown()
    {
        downConnection.enabled = true;
        _inUse = true;
    }

    public void DisconnectDown()
    {
        downConnection.enabled = false;
    }

    public void ConnectLeft()
    {
        leftConnection.enabled = true;
        _inUse = true;
    }

    public void DisconnectLeft()
    {
        leftConnection.enabled = false;
    }

    public void ConnectRight()
    {
        rightConnection.enabled = true;
        _inUse = true;
    }

    public void DisconnectRight()
    {
        rightConnection.enabled = false;
    }
    #endregion Connections

    #region Background
    public void Fill()
    {
        background.enabled = true;
        background.color = _color;
    }
    public void Empty()
    {
        background.enabled = false;
    }
    #endregion Background

    #region MiniCircle
    public void SpawnMiniCircle()
    {
        if (!_isCircle)
        {
            circle.transform.localScale = new Vector3(0.9f, 0.9f, 1.0f);
            circle.enabled = true;
            circle.color = _color;
        }
    }

    public void DespawnMiniCircle()
    {
        if(!_isCircle)
            circle.enabled = false;
    }
    #endregion MiniCircle

    #region Walls
    public void WallUp()
    {
        upWall.enabled = true;
        _walls[(int)WallsDirs.up] = true;
    }

    public void WallDown()
    {
        downWall.enabled = true;
        _walls[(int)WallsDirs.down] = true;
    }

    public void WallLeft()
    {
        leftWall.enabled = true;
        _walls[(int)WallsDirs.left] = true;
    }

    public void WallRight()
    {
        rightWall.enabled = true;
        _walls[(int)WallsDirs.right] = true;
    }

    public bool[] getWalls()
    {
        return _walls;
    }
    #endregion Walls

    #region Coords
    public void SetCoords(int i, int j)
    {
        _x = i;
        _y = j;
    }

    public void GetCoords(out int i, out int j)
    {
        i = _x;
        j = _y;
    }
    #endregion Coords

    public void SetAsHole()
    {
        border.enabled = false;
        _isHole = true;
    }

    public void ShowStar()
    {
        star.enabled = true;
    }
}