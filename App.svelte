<script lang="ts">
     import { ethers } from "ethers";
     import Web3 from "web3";

     type WalletProvider = {
          request: (args: {
               method: string;
               params?: unknown[];
          }) => Promise<unknown>;
          on?: (event: string, listener: (...args: unknown[]) => void) => void;
          removeListener?: (
               event: string,
               listener: (...args: unknown[]) => void,
          ) => void;
     };

     type WalletWindow = Window & {
          ethereum?: WalletProvider;
          pali?: WalletProvider;
          paliEthereum?: WalletProvider;
     };

     let address = "";
     let balanceEthers = "";
     let balanceWeb3 = "";
     let chainId = "";
     let status = "Wallet no conectada";
     let isLoading = false;

     const getProvider = (): WalletProvider | null => {
          const walletWindow = window as WalletWindow;
          return (
               walletWindow.pali ??
               walletWindow.paliEthereum ??
               walletWindow.ethereum ??
               null
          );
     };

     const shortAddress = (value: string) =>
          value ? `${value.slice(0, 6)}...${value.slice(-4)}` : "-";

     const connectWallet = async () => {
          const injected = getProvider();

          if (!injected) {
               status = "No se detectó Pali Wallet";
               return;
          }

          try {
               isLoading = true;
               status = "Conectando...";

               const browserProvider = new ethers.BrowserProvider(
                    injected as ethers.Eip1193Provider,
               );
               const accounts = (await injected.request({
                    method: "eth_requestAccounts",
               })) as string[];

               if (!accounts.length) {
                    status = "No se obtuvo ninguna cuenta";
                    return;
               }

               address = accounts[0];

               const [ethersBalanceRaw, web3BalanceRaw, network] =
                    await Promise.all([
                         browserProvider.getBalance(address),
                         new Web3(injected as never).eth.getBalance(address),
                         browserProvider.getNetwork(),
                    ]);

               balanceEthers = Number(
                    ethers.formatEther(ethersBalanceRaw),
               ).toFixed(6);
               balanceWeb3 = Number(
                    new Web3().utils.fromWei(web3BalanceRaw, "ether"),
               ).toFixed(6);
               chainId = network.chainId.toString();
               status = "Sesión iniciada correctamente";
          } catch {
               status = "No fue posible conectar con Pali Wallet";
          } finally {
               isLoading = false;
          }
     };
</script>

<main class="min-h-screen bg-[#f3f5f8] px-4 py-10 text-[#111827] sm:px-6">
     <section
          class="mx-auto w-full max-w-5xl rounded-2xl border border-[#d8dee8] bg-white p-6 shadow-[0_18px_40px_-24px_rgba(17,24,39,0.28)] sm:p-10"
     >
          <div
               class="mb-8 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between"
          >
               <div>
                    <p
                         class="mb-2 text-xs font-semibold uppercase tracking-[0.16em] text-[#4b5563]"
                    >
                         Svelte + Pali Wallet
                    </p>
                    <h1
                         class="text-3xl font-bold leading-tight text-[#0f172a] sm:text-4xl"
                    >
                         Inicio de sesión
                    </h1>
               </div>

               <button
                    class="rounded-xl bg-[#1f3a6e] px-6 py-3 text-sm font-semibold text-white transition hover:bg-[#19315d] disabled:cursor-not-allowed disabled:opacity-60"
                    on:click={connectWallet}
                    disabled={isLoading}
               >
                    {isLoading ? "Conectando..." : "Iniciar sesión con Pali"}
               </button>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
               <article
                    class="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-5"
               >
                    <h2
                         class="text-xs font-semibold uppercase tracking-[0.14em] text-[#64748b]"
                    >
                         Estado de sesión
                    </h2>
                    <p
                         class="mt-3 text-base font-semibold text-[#0f172a] sm:text-lg"
                    >
                         {status}
                    </p>
               </article>

               <article
                    class="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-5"
               >
                    <h2
                         class="text-xs font-semibold uppercase tracking-[0.14em] text-[#64748b]"
                    >
                         Dirección
                    </h2>
                    <p
                         class="mt-3 break-all text-sm font-medium text-[#0f172a] sm:text-base"
                    >
                         {address || "-"}
                    </p>
                    <p class="mt-2 text-xs text-[#64748b] sm:text-sm">
                         Vista corta: {shortAddress(address)}
                    </p>
               </article>

               <article
                    class="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-5"
               >
                    <h2
                         class="text-xs font-semibold uppercase tracking-[0.14em] text-[#64748b]"
                    >
                         Saldo con Ethers.js
                    </h2>
                    <p class="mt-3 text-2xl font-bold text-[#0f172a]">
                         {balanceEthers || "0.000000"} ETH
                    </p>
               </article>

               <article
                    class="rounded-xl border border-[#e5e7eb] bg-[#f8fafc] p-5"
               >
                    <h2
                         class="text-xs font-semibold uppercase tracking-[0.14em] text-[#64748b]"
                    >
                         Saldo con Web3.js
                    </h2>
                    <p class="mt-3 text-2xl font-bold text-[#0f172a]">
                         {balanceWeb3 || "0.000000"} ETH
                    </p>
                    <p class="mt-2 text-xs text-[#64748b] sm:text-sm">
                         Red activa (Chain ID): {chainId || "-"}
                    </p>
               </article>
          </div>
     </section>
</main>
