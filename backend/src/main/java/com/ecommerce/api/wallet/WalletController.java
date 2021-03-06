package com.ecommerce.api.wallet;

import com.ecommerce.application.IWalletService;
import com.ecommerce.domain.exception.EmptyListException;
import com.ecommerce.domain.exception.NotFoundException;
import com.ecommerce.domain.repository.entity.TransactionDonationHistory;
import com.ecommerce.domain.repository.entity.Wallet;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class WalletController {
	public static final Logger logger = LoggerFactory.getLogger(WalletController.class);

	private IWalletService walletService;

	@Autowired
	public WalletController(IWalletService walletService) {
		Assert.notNull(walletService, "walletService 개체가 반드시 필요!");
		this.walletService = walletService;
	}

	// test code
	/**
	 * 지갑 등록
	 * @param wallet
	 */
	@ApiOperation(value = "Register wallet of user")
	@RequestMapping(value = "/wallets", method = RequestMethod.POST)
	public Wallet register(@Valid @RequestBody Wallet wallet) {
		logger.debug("register start");
		logger.debug(wallet.getAddress());
		logger.debug(String.valueOf(wallet.getOwnerId()));

		this.walletService.register(wallet);
		Wallet newWallet = walletService.getAndSyncBalance(wallet.getAddress());
		if(newWallet == null)
			throw new NotFoundException(wallet.getAddress() + " 해당 주소 지갑을 찾을 수 없습니다.");

		logger.debug(newWallet.toString());
		logger.debug("register end");
		return newWallet;
	}

	/**
	 * 지갑 조회 by address
	 * @param address 지갑 주소
	 */
	@ApiOperation(value = "Fetch wallet by address")
	@RequestMapping(value = "/wallets/{address}", method = RequestMethod.GET)
	public Wallet get(@PathVariable String address) {
		return walletService.getAndSyncBalance(address);
	}

	/**
	 * 지갑 조회 by user's id
	 * @param uid 사용자 id
	 */
	@ApiOperation(value = "Fetch wallet of user")
	@RequestMapping(value = "/wallets/of/{uid}", method = RequestMethod.GET)
	public Wallet getByUser(@PathVariable long uid) {
		Wallet wallet = this.walletService.get(uid);
		if(wallet == null)
			throw new EmptyListException("[UserId] " + uid + " 해당 지갑을 찾을 수 없습니다.");
		return walletService.getAndSyncBalance(wallet.getAddress());
	}


	/**
	 * Donation/Token 거래 기록을 저장한다.
	 * @param transactionDonationHistory
	 * @return
	 */
	@ApiOperation(value = "Register Donation History")
	@RequestMapping(value = "/wallets/donation", method = RequestMethod.POST)
	public TransactionDonationHistory createDonation(@Valid @RequestBody TransactionDonationHistory transactionDonationHistory) {
		logger.debug("createDonation START");
		return walletService.createDonation(transactionDonationHistory);
	}



	@ApiOperation(value = "Get Donation History of User")
	@RequestMapping(value = "/wallets/donation/of/{address}", method = RequestMethod.GET)
	public List<TransactionDonationHistory> getDonationByAddress(@PathVariable String address) {
		return walletService.getDonationList(address);
	}


	/**
	 *  충전 요청
	 * @param address 지갑 주소
	 */
	@ApiOperation(value = "Request ether")
	@RequestMapping(value ="/wallets/{address}", method = RequestMethod.PUT)
	public Wallet requestEth(@PathVariable String address){ // 테스트 가능하도록 일정 개수의 코인을 충전해준다.

		return this.walletService.requestEth(address);
	}

	@ApiOperation(value = "Fetch all wallets")
	@RequestMapping(value = "/wallets", method = RequestMethod.GET)
	public List<Wallet> list() {
		List<Wallet> list = walletService.list();

		if (list == null || list.isEmpty() )
			throw new EmptyListException("NO DATA");

		return list;
	}

}
