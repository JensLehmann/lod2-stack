/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.lod2.stat.datacube;

import java.util.Collection;

/**
 *
 * @author vukm
 */
public interface CodeList extends Thing {
    
    public Collection<String> getAllCodes();
    public Collection<String> codesOnLevel(int level);
    public int numCodeLevels();
    
}
