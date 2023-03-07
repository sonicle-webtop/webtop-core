/*
 * Copyright (C) 2022 Sonicle S.r.l.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY SONICLE, SONICLE DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Sonicle S.r.l. at email address sonicle[at]sonicle[dot]com
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * Sonicle logo and Sonicle copyright notice. If the display of the logo is not
 * reasonably feasible for technical reasons, the Appropriate Legal Notices must
 * display the words "Copyright (C) 2022 Sonicle S.r.l.".
 */
package com.sonicle.webtop.core.app;

import com.sonicle.commons.l4j.ProductLicense;
import com.sonicle.webtop.core.app.sdk.WTNotFoundException;
import com.sonicle.webtop.core.app.util.ClassHelper;
import com.sonicle.webtop.core.model.ProductId;
import com.sonicle.webtop.core.sdk.BaseServiceProduct;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author malbinola
 */
public class ProductRegistry {
	private static final ProductRegistry instance;
	static {
		instance = new ProductRegistry();
	}
	
	public static ProductRegistry getInstance() {
		return instance;
	}
	
	private final ConcurrentHashMap<String, ProductEntry> products;
	
	private ProductRegistry() {
		this.products = new ConcurrentHashMap<>();
	}
	
	public final ProductEntry register(final String productClassName) {
		// Instantiate class to check object existance
		BaseServiceProduct product = instantiateProduct(productClassName, WebTopManager.SYSADMIN_DOMAINID);
		if (product == null) return null;
		ProductEntry newEntry = new ProductEntry(productClassName, product.getProductCode(), product.getProductName());
		ProductEntry oldEntry = products.putIfAbsent(product.getProductCode(), newEntry);
		return (oldEntry != null) ? null : newEntry;
	}
	
	public final ProductEntry getProductOrThrow(final String productCode) throws WTNotFoundException {
		final ProductEntry entry = getProduct(productCode);
		if (entry == null) throw new WTNotFoundException("Unknown product [{}]", productCode);
		return entry;
	}
	
	public final ProductEntry getProduct(final String productCode) {
		return products.get(productCode);
	}
	
	public final BaseServiceProduct getServiceProductOrThrow(final String productCode, final String domainId) throws WTNotFoundException {
		final BaseServiceProduct product = getServiceProduct(productCode, domainId);
		if (product == null) throw new WTNotFoundException("Unknown product [{}]", productCode);
		return product;
	}
	
	public final BaseServiceProduct getServiceProduct(final String productCode, final String domainId) {
		ProductEntry product = products.get(productCode);
		if (product == null) return null;
		return instantiateProduct(product.getClassName(), domainId);
	}
	
	public final BaseServiceProduct getServiceProduct(final ProductId productId, final String domainId) {
		return getServiceProduct(productId.getProductCode(), domainId);
	}
	
	public static BaseServiceProduct instantiateProduct(final String className, final String domainId) {
		Class clazz = ClassHelper.loadClass(className, null);
		if (clazz == null) return null;
		
		try {
			BaseServiceProduct product = null;
			if (ClassHelper.isInheritingFromParent(clazz, BaseServiceProduct.class)) {
				product = (BaseServiceProduct)clazz.getDeclaredConstructor(String.class).newInstance(domainId);
			}/* else if (ClassHelper.isInheritingFromParent(clazz, AbstractProduct.class)) {
				product = (AbstractProduct)clazz.getDeclaredConstructor().newInstance();
			}*/ else {
				return null;
			}
			return product;
			
		} catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			return null;
		}
	}
	
	public static class ProductEntry {
		private final String className;
		private final String productCode;
		private final String name;
		
		public ProductEntry(String className, String productCode, String name) {
			this.className = className;
			this.productCode = productCode;
			this.name = name;
		}

		public String getClassName() {
			return className;
		}

		public String getProductCode() {
			return productCode;
		}

		public String getName() {
			return name;
		}
		
		public String getServiceId() {
			return WT.findServiceId(className);
		}
		
		public ProductId getProductId() {
			return ProductId.build(getServiceId(), productCode);
		}
	}
}
