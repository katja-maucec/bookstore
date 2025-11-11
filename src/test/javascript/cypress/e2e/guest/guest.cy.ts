describe('Guest user e2e test', () => {
  const homeUrl = '/';
  const loginUrl = '/login';
  const registerUrl = '/account/register';
  const bookstoreUrl = '/book';

  beforeEach(() => {
    cy.visit(homeUrl);
  });

  //
  // NAVBAR VISIBILITY TESTS
  //
  describe('Navbar visibility', () => {
    it('should show Home and Login in navbar', () => {
      cy.get('[data-cy="navbar"]').within(() => {
        cy.contains('Home').should('exist');
        cy.contains('Login').should('exist');
      });
    });

    it('should not show Cart or Orders in navbar', () => {
      cy.get('[data-cy="navbar"]').within(() => {
        cy.contains('Cart').should('not.exist');
        cy.contains('Orders').should('not.exist');
      });
    });
  });

  //
  // NAVIGATION TESTS
  //
  describe('Navigation', () => {
    it('should navigate to bookstore when clicking "Browse all books"', () => {
      // Wait for Angular and the page to render
      cy.get('body').should('exist');

      // Wait until the button exists and is visible
      cy.get('[data-cy="browse-all-books-button"]').should('exist').should('be.visible').click();

      // Ensure we navigated to bookstore
      cy.url().should('include', '/book');
    });

    it('should navigate to Sign In page', () => {
      // Wait for navbar to be rendered
      cy.get('[data-cy="navbar"]', { timeout: 10000 }).should('exist');

      // Click the account menu to open dropdown
      cy.get('[data-cy="accountMenu"]', { timeout: 10000 }).should('exist').should('be.visible').click();

      // Click the "Sign in" item in the dropdown
      cy.get('[data-cy="login"]', { timeout: 10000 }).should('exist').should('be.visible').click();

      // Verify URL and page content
      cy.url({ timeout: 10000 }).should('include', '/login');
      cy.contains('Sign in').should('exist');
    });

    it('should navigate to Register page', () => {
      // Wait for navbar to be rendered
      cy.get('[data-cy="navbar"]', { timeout: 10000 }).should('exist');

      // Click the account menu to open dropdown
      cy.get('[data-cy="accountMenu"]', { timeout: 10000 }).should('exist').should('be.visible').click();

      // Click the register link
      cy.get('[data-cy="register"]', { timeout: 10000 }).should('exist').should('be.visible').click();

      // Verify URL and page content
      cy.url({ timeout: 10000 }).should('include', '/account/register');
      cy.contains('Register').should('exist');
    });
  });

  //
  // BOOK DETAILS PAGE
  //
  describe('Book details page', () => {
    beforeEach(() => {
      cy.visit(bookstoreUrl);

      // Wait until the books table is rendered
      cy.get('[data-cy="entityTable"]').should('exist');

      // Click the first "View" button (view details)
      cy.get('[data-cy="entityDetailsButton"]').first().click();
    });

    it('should show book information but no review or add-to-cart buttons', () => {
      // Check that the book title is shown (h1/h2/h3 is fine)
      cy.get('h1, h2, h3').should('exist');

      // Ensure guest cannot see review or add-to-cart
      cy.get('[data-cy="entityLeaveReviewButton"]').should('not.exist');
      cy.get('[data-cy="entityAddToCartButton"]').should('not.exist');
    });
  });

  //
  // AUTHENTICATION TESTS
  //
  describe('Authentication flow', () => {
    it('should allow signing in with default user account', () => {
      cy.visit(loginUrl);
      cy.get('input[name="username"]').type('user');
      cy.get('input[name="password"]').type('user');
      cy.get('button[type="submit"]').click();

      // after login, should see cart or orders now
      cy.get('nav').within(() => {
        cy.contains('Cart').should('exist');
        cy.contains('Orders').should('exist');
      });
    });

    it('should allow navigating to register and creating new account', () => {
      cy.visit(registerUrl);

      const randomUser = `test_${Date.now()}`;

      cy.get('input[name="login"]').type(randomUser);
      cy.get('input[name="password"]').type('Test1234!');
      cy.get('input[name="confirmPassword"]').type('Test1234!');

      cy.get('button[type="submit"]').click();

      // Expect redirect or success message
      cy.contains('Registration saved!').should('exist');
    });
  });
});
