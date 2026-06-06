(function () {
  const { brand, globalBenefits, products, steps } = window.STORE;

  const discordLinks = [
    'discord-cta-top',
    'discord-cta-hero',
    'discord-cta-bottom',
  ];

  discordLinks.forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.href = brand.discord;
  });

  document.getElementById('year').textContent = new Date().getFullYear();
  document.getElementById('stat-products').textContent = products.length;

  const benefitsGrid = document.getElementById('benefits-grid');
  globalBenefits.forEach((item) => {
    const card = document.createElement('article');
    card.className = 'benefit-card';
    card.innerHTML = `
      <div class="benefit-card__icon">${item.icon}</div>
      <h3>${item.title}</h3>
      <p>${item.text}</p>
    `;
    benefitsGrid.appendChild(card);
  });

  const productsGrid = document.getElementById('products-grid');
  let activeFilter = 'all';

  function productCategory(product) {
    if (product.id === 'economia') return 'economia';
    if (product.duration.toLowerCase().includes('30')) return 'temporario';
    return 'permanente';
  }

  function renderProducts() {
    productsGrid.innerHTML = '';
    const filtered = products.filter((p) => {
      if (activeFilter === 'all') return true;
      return productCategory(p) === activeFilter;
    });

    filtered.forEach((product) => {
      const card = document.createElement('article');
      card.className = 'product-card';
      card.style.borderTop = `3px solid ${product.color}`;
      card.innerHTML = `
        <div class="product-card__top">
          <h3 class="product-card__name" style="color:${product.color}">${product.name}</h3>
          <span class="product-card__badge">${product.badge}</span>
        </div>
        <p class="product-card__duration">${product.duration}</p>
        <p class="product-card__highlight">${product.highlight}</p>
        <ul class="product-card__includes">
          ${product.includes.map((line) => `<li style="color:${product.color}">${line}</li>`).join('')}
        </ul>
        <div class="product-card__footer">
          <span class="product-card__via">Compra via ticket Discord</span>
        </div>
      `;
      productsGrid.appendChild(card);
    });
  }

  document.querySelectorAll('.filter').forEach((btn) => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter').forEach((b) => b.classList.remove('is-active'));
      btn.classList.add('is-active');
      activeFilter = btn.dataset.filter;
      renderProducts();
    });
  });

  renderProducts();

  const stepsList = document.getElementById('steps-list');
  steps.forEach((text, index) => {
    const step = document.createElement('article');
    step.className = 'step-card';
    step.innerHTML = `
      <div class="step-card__num">${index + 1}</div>
      <p>${text}</p>
    `;
    stepsList.appendChild(step);
  });

  const menuToggle = document.getElementById('menu-toggle');
  const headerInner = document.querySelector('.header__inner');
  menuToggle?.addEventListener('click', () => {
    headerInner.classList.toggle('nav-open');
  });
})();
