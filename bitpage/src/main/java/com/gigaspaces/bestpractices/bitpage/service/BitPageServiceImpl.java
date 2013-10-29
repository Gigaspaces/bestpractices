package com.gigaspaces.bestpractices.bitpage.service;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;

import com.gigaspaces.bestpractices.bitpage.BitPage;
import com.gigaspaces.query.IdQuery;

@RemotingService
public class BitPageServiceImpl implements BitPageService {
	@GigaSpaceContext
	private GigaSpace space;

	public Boolean exists(Integer val) {
		if(val==null)throw new IllegalArgumentException("null val supplied");
		int routing=new BitPage().getPageId(val);
		BitPage page=space.readById(new IdQuery<BitPage>(BitPage.class,routing,routing));
		if(page==null)return false;
		return page.isBitSet(val);
	}

	public void clear(Integer val) {
		if(val==null)throw new IllegalArgumentException("null val supplied");
		
		int routing=new BitPage().getPageId(val);
		BitPage page=space.readById(new IdQuery<BitPage>(BitPage.class,routing,routing));
		if(page==null)return;
		page.clearBit(val);
		space.write(page);
	}

	public Boolean set(Integer val) {
		if(val==null)throw new IllegalArgumentException("null val supplied");
		
		int routing=new BitPage().getPageId(val);
		BitPage page=space.readById(new IdQuery<BitPage>(BitPage.class,routing,routing));
		boolean ret=false;
		if(page==null){  //new one
			page=new BitPage(val);
		}
		else{
			ret=page.isBitSet(val);
		}
		page.setBit(val);
		space.write(page);
		return ret;
	}

}
