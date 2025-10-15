import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Book e2e test', () => {
  const bookPageUrl = '/book';
  const bookPageUrlPattern = new RegExp('/book(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const bookSample = { title: 'ectoderm', author: 'quip exactly', price: 12977.42, available: true };

  let book;
  let category;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // create an instance at the required relationship entity:
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/categories',
      body: { name: 'because near' },
    }).then(({ body }) => {
      category = body;
    });
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/books+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/books').as('postEntityRequest');
    cy.intercept('DELETE', '/api/books/*').as('deleteEntityRequest');
  });

  beforeEach(() => {
    // Simulate relationships api for better performance and reproducibility.
    cy.intercept('GET', '/api/reviews', {
      statusCode: 200,
      body: [],
    });

    cy.intercept('GET', '/api/categories', {
      statusCode: 200,
      body: [category],
    });
  });

  afterEach(() => {
    if (book) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/books/${book.id}`,
      }).then(() => {
        book = undefined;
      });
    }
  });

  afterEach(() => {
    if (category) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/categories/${category.id}`,
      }).then(() => {
        category = undefined;
      });
    }
  });

  it('Books menu should load Books page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('book');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Book').should('exist');
    cy.url().should('match', bookPageUrlPattern);
  });

  describe('Book page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(bookPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Book page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/book/new$'));
        cy.getEntityCreateUpdateHeading('Book');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bookPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/books',
          body: {
            ...bookSample,
            category,
          },
        }).then(({ body }) => {
          book = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/books+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              body: [book],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(bookPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Book page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('book');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bookPageUrlPattern);
      });

      it('edit button click should load edit Book page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Book');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bookPageUrlPattern);
      });

      it('edit button click should load edit Book page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Book');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bookPageUrlPattern);
      });

      it('last delete button click should delete instance of Book', () => {
        cy.get(entityDeleteButtonSelector).last().click();
        cy.getEntityDeleteDialogHeading('book').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', bookPageUrlPattern);

        book = undefined;
      });
    });
  });

  describe('new Book page', () => {
    beforeEach(() => {
      cy.visit(`${bookPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Book');
    });

    it('should create an instance of Book', () => {
      cy.get(`[data-cy="title"]`).type('gosh bolster');
      cy.get(`[data-cy="title"]`).should('have.value', 'gosh bolster');

      cy.get(`[data-cy="author"]`).type('that unimpressively spice');
      cy.get(`[data-cy="author"]`).should('have.value', 'that unimpressively spice');

      cy.get(`[data-cy="description"]`).type('circa beautifully greedily');
      cy.get(`[data-cy="description"]`).should('have.value', 'circa beautifully greedily');

      cy.get(`[data-cy="price"]`).type('24739.11');
      cy.get(`[data-cy="price"]`).should('have.value', '24739.11');

      cy.get(`[data-cy="available"]`).should('not.be.checked');
      cy.get(`[data-cy="available"]`).click();
      cy.get(`[data-cy="available"]`).should('be.checked');

      cy.get(`[data-cy="category"]`).select(1);

      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        book = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', bookPageUrlPattern);
    });
  });
});
