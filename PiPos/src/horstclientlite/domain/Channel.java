/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package horstclientlite.domain;

/**
 *
 * @author Gof
 */
public class Channel {
    private int id;
    private int frequency;
    
    public Channel(int id, int freq) {
        this.id = id;
        this.frequency = freq;
    }
}
