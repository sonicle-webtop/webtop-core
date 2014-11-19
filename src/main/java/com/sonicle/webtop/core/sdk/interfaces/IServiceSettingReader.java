/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sonicle.webtop.core.sdk.interfaces;

/**
 *
 * @author malbinola
 */
public interface IServiceSettingReader {
	
	public String getServiceSetting(String domainId, String serviceId, String key);
}