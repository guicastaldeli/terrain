package main.com.app.root.utils;
import org.joml.Vector3f;

public class TreeColors {
    public static Vector3f getColorForLevel(int level) {
        switch(level) {
            case 1:  return HexToVec3.hexToVec3("#65c767");
            case 2:  return HexToVec3.hexToVec3("#e3b81e");
            case 3:  return HexToVec3.hexToVec3("#1a7fba");
            case 4:  return HexToVec3.hexToVec3("#a91dc2");
            case 5:  return HexToVec3.hexToVec3("#34c2a8");
            case 6:  return HexToVec3.hexToVec3("#e309c2");
            case 7:  return HexToVec3.hexToVec3("#a1935f");
            case 8:  return HexToVec3.hexToVec3("#6c576e");
            case 9:  return HexToVec3.hexToVec3("#a32439");
            case 10: return HexToVec3.hexToVec3("#0f0f0f");
            default: return new Vector3f(1.0f, 1.0f, 1.0f);
        }
    }
}